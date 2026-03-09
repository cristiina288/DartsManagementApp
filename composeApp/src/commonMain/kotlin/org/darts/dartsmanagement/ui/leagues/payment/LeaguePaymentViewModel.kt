package org.darts.dartsmanagement.ui.leagues.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.darts.dartsmanagement.data.auth.SessionManager
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.models.BarModel
import org.darts.dartsmanagement.domain.leagues.GetActiveLeaguesUseCase
import org.darts.dartsmanagement.domain.leagues.SaveLeagueCollectionUseCase
import org.darts.dartsmanagement.domain.leagues.models.LeagueBarModel
import org.darts.dartsmanagement.domain.leagues.models.LeagueCollectionModel
import org.darts.dartsmanagement.domain.leagues.models.LeagueModel
import org.darts.dartsmanagement.domain.leagues.models.LeagueTeamModel

data class TeamDisplayModel(
    val teamId: String,
    val teamName: String,
    val barName: String?,
    val barId: String?
)

data class LeaguePaymentState(
    val isLoading: Boolean = false,
    val leagues: List<LeagueModel> = emptyList(),
    val selectedLeague: LeagueModel? = null,
    val searchMode: SearchMode = SearchMode.BAR,
    val selectedBar: BarModel? = null,
    val selectedTeam: TeamDisplayModel? = null,
    val amount: String = "",
    val paymentMethod: String = "CASH",
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val barsInLeague: List<BarModel> = emptyList(),
    val teamsInLeague: List<TeamDisplayModel> = emptyList(),
    val teamsForSelectedBar: List<TeamDisplayModel> = emptyList(),
    val barSearchQuery: String = "",
    val teamSearchQuery: String = ""
)

enum class SearchMode { BAR, TEAM }

sealed interface LeaguePaymentEvent {
    data class OnLeagueSelected(val league: LeagueModel?) : LeaguePaymentEvent
    data class OnSearchModeChanged(val mode: SearchMode) : LeaguePaymentEvent
    data class OnBarSelected(val bar: BarModel?) : LeaguePaymentEvent
    data class OnTeamSelected(val team: TeamDisplayModel?) : LeaguePaymentEvent
    data class OnAmountChanged(val amount: String) : LeaguePaymentEvent
    data class OnPaymentMethodChanged(val method: String) : LeaguePaymentEvent
    data object OnSavePayment : LeaguePaymentEvent
    data class OnBarSearchQueryChanged(val query: String) : LeaguePaymentEvent
    data class OnTeamSearchQueryChanged(val query: String) : LeaguePaymentEvent
}

class LeaguePaymentViewModel(
    private val getActiveLeaguesUseCase: GetActiveLeaguesUseCase,
    private val getBarsUseCase: GetBars,
    private val saveLeagueCollectionUseCase: SaveLeagueCollectionUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaguePaymentState())
    val uiState = _uiState.asStateFlow()

    init {
        loadLeagues()
    }

    private fun loadLeagues() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val leagues = withContext(Dispatchers.IO) { getActiveLeaguesUseCase() }
                _uiState.update { it.copy(leagues = leagues, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onEvent(event: LeaguePaymentEvent) {
        when (event) {
            is LeaguePaymentEvent.OnLeagueSelected -> handleLeagueSelected(event.league)
            is LeaguePaymentEvent.OnSearchModeChanged -> _uiState.update { 
                it.copy(searchMode = event.mode, selectedBar = null, selectedTeam = null, barSearchQuery = "", teamSearchQuery = "", amount = "", error = null) 
            }
            is LeaguePaymentEvent.OnBarSelected -> handleBarSelected(event.bar)
            is LeaguePaymentEvent.OnTeamSelected -> handleTeamSelected(event.team)
            is LeaguePaymentEvent.OnAmountChanged -> handleAmountChanged(event.amount)
            is LeaguePaymentEvent.OnPaymentMethodChanged -> _uiState.update { it.copy(paymentMethod = event.method) }
            LeaguePaymentEvent.OnSavePayment -> savePayment()
            is LeaguePaymentEvent.OnBarSearchQueryChanged -> _uiState.update { it.copy(barSearchQuery = event.query) }
            is LeaguePaymentEvent.OnTeamSearchQueryChanged -> _uiState.update { it.copy(teamSearchQuery = event.query) }
        }
    }

    private fun handleLeagueSelected(league: LeagueModel?) {
        if (league == null) {
            _uiState.update { LeaguePaymentState(leagues = it.leagues) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedLeague = league, selectedBar = null, selectedTeam = null, amount = "", error = null) }
            try {
                val allBars = withContext(Dispatchers.IO) { getBarsUseCase() }
                
                val barsInLeague: List<BarModel>
                val teamsInLeague: List<TeamDisplayModel>

                if (league.ownerPayment == "BAR") {
                    val pendingBarIds = league.bars
                        .filter { it.barFinances.paymentStatus == "PENDING" }
                        .map { it.barId }
                    barsInLeague = allBars.filter { it.id in pendingBarIds }
                    teamsInLeague = emptyList()
                } else {
                    // ownerPayment == "TEAM"
                    val teamsPending = league.bars.flatMap { leagueBar ->
                        val barName = allBars.find { it.id == leagueBar.barId }?.name
                        leagueBar.teams
                            .filter { it.teamFinances.paymentStatus == "PENDING" }
                            .map { team ->
                                TeamDisplayModel(
                                    teamId = team.teamId,
                                    teamName = team.teamName,
                                    barName = barName,
                                    barId = leagueBar.barId
                                )
                            }
                    }
                    teamsInLeague = teamsPending
                    
                    val barIdsWithPendingTeams = teamsPending.mapNotNull { it.barId }.distinct()
                    barsInLeague = allBars.filter { it.id in barIdsWithPendingTeams }
                }

                _uiState.update { it.copy(
                    barsInLeague = barsInLeague,
                    teamsInLeague = teamsInLeague,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun handleBarSelected(bar: BarModel?) {
        val teams = if (bar != null) {
            _uiState.value.teamsInLeague.filter { it.barId == bar.id }
        } else emptyList()
        
        _uiState.update { it.copy(
            selectedBar = bar, 
            selectedTeam = null, 
            teamsForSelectedBar = teams,
            amount = "",
            error = null
        ) }
    }

    private fun handleTeamSelected(team: TeamDisplayModel?) {
        _uiState.update { it.copy(selectedTeam = team, amount = "", error = null) }
    }

    private fun handleAmountChanged(amount: String) {
        val amountValue = amount.toDoubleOrNull() ?: 0.0
        val pending = getPendingAmount()
        
        val error = if (amountValue > pending) {
            "El precio no puede superar lo que le queda por pagar que es: $pending €"
        } else null
        
        _uiState.update { it.copy(amount = amount, error = error) }
    }

    private fun getPendingAmount(): Double {
        val state = _uiState.value
        val league = state.selectedLeague ?: return 0.0
        
        return if (league.ownerPayment == "BAR") {
            state.selectedBar?.let { bar ->
                league.bars.find { it.barId == bar.id }?.barFinances?.amountPending
            } ?: 0.0
        } else {
            // TEAM payment
            if (state.searchMode == SearchMode.BAR) {
                state.selectedTeam?.let { team ->
                    findTeamFinances(league, team.barId, team.teamId)?.amountPending
                } ?: 0.0
            } else {
                state.selectedTeam?.let { team ->
                    findTeamFinances(league, team.barId, team.teamId)?.amountPending
                } ?: 0.0
            }
        }
    }

    private fun findTeamFinances(league: LeagueModel, barId: String?, teamId: String) = 
        league.bars.find { it.barId == barId }?.teams?.find { it.teamId == teamId }?.teamFinances

    private fun savePayment() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: 0.0
        if (amount <= 0 || state.error != null) return

        val payeeId = if (state.selectedLeague?.ownerPayment == "BAR") {
            state.selectedBar?.id
        } else {
            state.selectedTeam?.teamId
        } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val licenseId = sessionManager.licenseId.value ?: throw IllegalStateException("No license")
                val userId = sessionManager.userId.value ?: throw IllegalStateException("No user")
                
                val leagueCollection = LeagueCollectionModel(
                    licenseId = licenseId,
                    leagueId = state.selectedLeague!!.id,
                    amount = amount,
                    collectionId = "DIRECT_PAYMENT", // Indicated direct payment
                    payeeId = payeeId,
                    method = state.paymentMethod,
                    recordedBy = userId
                )
                
                withContext(Dispatchers.IO) {
                    saveLeagueCollectionUseCase(leagueCollection)
                }
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
