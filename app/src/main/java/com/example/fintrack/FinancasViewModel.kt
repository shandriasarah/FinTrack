package com.example.fintrack

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class FinancasViewModel : ViewModel() {
    // Lista inicial já preenchida para testar o visual novo
    val transacoes = mutableStateListOf(
        Transacao(1, "Salário", 3500.0, TipoTransacao.ENTRADA, Categoria.SALARIO),
        Transacao(2, "Mercado", 187.60, TipoTransacao.SAIDA, Categoria.MERCADO),
        Transacao(3, "Transporte", 15.40, TipoTransacao.SAIDA, Categoria.TRANSPORTE),
        Transacao(4, "Freelance", 800.0, TipoTransacao.ENTRADA, Categoria.FREELANCE),
        Transacao(5, "Restaurante", 89.90, TipoTransacao.SAIDA, Categoria.RESTAURANTE)
    )

    fun adicionarTransacao(titulo: String, valor: Double, tipo: TipoTransacao, categoria: String) {
        val nova = Transacao(
            id = (transacoes.maxOfOrNull { it.id } ?: 0) + 1,
            titulo = titulo,
            valor = valor,
            tipo = tipo,
            categoria = if (tipo == TipoTransacao.ENTRADA) Categoria.SALARIO else Categoria.MERCADO
        )
        transacoes.add(0, nova)
    }
    fun adicionarTransacao(descricao: String, valor: Double, tipo: TipoTransacao, categoria: Categoria) {
        val novaTransacao = Transacao(
            id = (transacoes.maxOfOrNull { it.id } ?: 0) + 1,
            descricao = descricao,
            valor = valor,
            tipo = tipo,
            categoria = categoria,
            data = "Hoje"
        )
        transacoes.add(0, novaTransacao) // Adiciona no topo da lista
    }

    fun removerTransacao(transacao: Transacao) {
        transacoes.remove(transacao)
    }

    fun calcularSaldo(): Double = calcularTotalEntradas() - calcularTotalSaidas()

    fun calcularTotalEntradas(): Double = transacoes.filter { it.tipo == TipoTransacao.ENTRADA }.sumOf { it.valor }

    fun calcularTotalSaidas(): Double = transacoes.filter { it.tipo == TipoTransacao.SAIDA }.sumOf { it.valor }
}