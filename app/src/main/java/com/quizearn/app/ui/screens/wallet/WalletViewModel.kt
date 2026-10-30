package com.quizearn.app.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.WalletRepository
import com.quizearn.app.data.repository.WalletRepositoryImpl
import com.quizearn.app.domain.model.Redemption
import com.quizearn.app.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class TransactionFilter {
    ALL,
    CREDITS,
    DEBITS
}

/**
 * ViewModel governing reactive points balance derivation from immutable ledger stream,
 * transaction history filtering, and redemption execution.
 */
class WalletViewModel(
    private val walletRepository: WalletRepository = WalletRepositoryImpl.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val _currentFilter = MutableStateFlow(TransactionFilter.ALL)
    val currentFilter: StateFlow<TransactionFilter> = _currentFilter.asStateFlow()

    private var rawTransactions: List<TransactionItem> = emptyList()

    init {
        observeBalance()
        observeTransactions()
    }

    private fun observeBalance() {
        walletRepository.getBalance()
            .onEach { balance ->
                val monetary = "$${String.format(java.util.Locale.US, "%.2f", balance.toDouble() / 1000.0)}"
                _uiState.update {
                    it.copy(
                        pointsBalance = balance.toInt(),
                        monetaryEquivalentFormatted = monetary,
                        canRedeem = balance >= it.minRedemptionThreshold
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTransactions() {
        walletRepository.getTransactions()
            .onEach { result ->
                if (result is Result.Success) {
                    rawTransactions = result.data.map { mapToTransactionItem(it) }
                    applyFilter(_currentFilter.value)
                }
            }
            .launchIn(viewModelScope)
    }

    fun setFilter(filter: TransactionFilter) {
        _currentFilter.value = filter
        applyFilter(filter)
    }

    private fun applyFilter(filter: TransactionFilter) {
        val filtered = when (filter) {
            TransactionFilter.ALL -> rawTransactions
            TransactionFilter.CREDITS -> rawTransactions.filter { it.isCredit }
            TransactionFilter.DEBITS -> rawTransactions.filter { !it.isCredit }
        }
        _uiState.update { it.copy(recentTransactions = filtered) }
    }

    fun requestRedemption(
        rewardId: String,
        payoutDetails: String,
        idempotencyKey: String = UUID.randomUUID().toString(),
        onResult: (Result<Redemption>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = walletRepository.requestRedemption(rewardId, payoutDetails, idempotencyKey)
            onResult(res)
        }
    }

    private fun mapToTransactionItem(txn: Transaction): TransactionItem {
        val isCredit = txn.type.name == "CREDIT"
        val type = when (txn.reason.name) {
            "QUIZ_REWARD" -> TransactionType.QUIZ_REWARD
            "DAILY_CHECKIN" -> TransactionType.DAILY_CHECKIN
            "TASK_COMPLETED" -> TransactionType.TASK_COMPLETED
            "TOURNAMENT_PRIZE" -> TransactionType.TOURNAMENT_PRIZE
            else -> TransactionType.REDEMPTION
        }
        return TransactionItem(
            id = txn.id,
            type = type,
            title = txn.description,
            timestampFormatted = txn.timestampIso,
            pointsDelta = if (isCredit) txn.amount.toInt() else -txn.amount.toInt(),
            isCredit = isCredit,
            status = TransactionStatus.COMPLETED
        )
    }
}
