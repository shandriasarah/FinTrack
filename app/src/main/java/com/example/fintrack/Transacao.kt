package com.example.fintrack

import androidx.compose.ui.graphics.Color

enum class TipoTransacao {
    ENTRADA,
    SAIDA
}

/**
 * Grupos da regra 50/30/20:
 * - ESSENCIAL: o que você precisa pagar para viver (moradia, mercado, transporte...)
 * - ESTILO_DE_VIDA: o que é escolha (restaurante, lazer, compras...)
 * - FUTURO: dinheiro guardado para metas e reserva
 */
enum class GrupoOrcamento(val nome: String, val cor: Color) {
    ESSENCIAL("Essenciais", Color(0xFF2F6FDE)),
    ESTILO_DE_VIDA("Estilo de vida", Color(0xFFF08C2E)),
    FUTURO("Futuro", Color(0xFF00A673)),
    RENDA("Renda", Color(0xFF00A673))
}

enum class Categoria(
    val nome: String,
    val emoji: String,
    val cor: Color,
    val tipo: TipoTransacao,
    val grupo: GrupoOrcamento
) {
    // Entradas
    SALARIO("Salário", "💼", Color(0xFF00A673), TipoTransacao.ENTRADA, GrupoOrcamento.RENDA),
    FREELANCE("Freelance", "💻", Color(0xFF26A69A), TipoTransacao.ENTRADA, GrupoOrcamento.RENDA),
    RENDIMENTOS("Rendimentos", "📈", Color(0xFF43A047), TipoTransacao.ENTRADA, GrupoOrcamento.RENDA),
    OUTRAS_ENTRADAS("Outras entradas", "💰", Color(0xFF7CB342), TipoTransacao.ENTRADA, GrupoOrcamento.RENDA),

    // Saídas essenciais
    MORADIA("Moradia", "🏠", Color(0xFF3F51B5), TipoTransacao.SAIDA, GrupoOrcamento.ESSENCIAL),
    MERCADO("Mercado", "🛒", Color(0xFF2F6FDE), TipoTransacao.SAIDA, GrupoOrcamento.ESSENCIAL),
    CONTAS("Contas da casa", "⚡", Color(0xFF00ACC1), TipoTransacao.SAIDA, GrupoOrcamento.ESSENCIAL),
    TRANSPORTE("Transporte", "🚌", Color(0xFF5C6BC0), TipoTransacao.SAIDA, GrupoOrcamento.ESSENCIAL),
    SAUDE("Saúde", "💊", Color(0xFFE53935), TipoTransacao.SAIDA, GrupoOrcamento.ESSENCIAL),
    EDUCACAO("Educação", "📚", Color(0xFF8E24AA), TipoTransacao.SAIDA, GrupoOrcamento.ESSENCIAL),

    // Saídas de estilo de vida
    RESTAURANTE("Restaurante", "🍔", Color(0xFFF08C2E), TipoTransacao.SAIDA, GrupoOrcamento.ESTILO_DE_VIDA),
    LAZER("Lazer", "🎬", Color(0xFFEC407A), TipoTransacao.SAIDA, GrupoOrcamento.ESTILO_DE_VIDA),
    COMPRAS("Compras", "🛍️", Color(0xFFAB47BC), TipoTransacao.SAIDA, GrupoOrcamento.ESTILO_DE_VIDA),
    ASSINATURAS("Assinaturas", "📺", Color(0xFFD81B60), TipoTransacao.SAIDA, GrupoOrcamento.ESTILO_DE_VIDA),
    OUTROS("Outros", "📦", Color(0xFF78909C), TipoTransacao.SAIDA, GrupoOrcamento.ESTILO_DE_VIDA),

    // Dinheiro guardado
    RESERVA("Guardado", "🐷", Color(0xFF00A673), TipoTransacao.SAIDA, GrupoOrcamento.FUTURO);

    companion object {
        fun doTipo(tipo: TipoTransacao): List<Categoria> =
            entries.filter { it.tipo == tipo && it != RESERVA }

        fun porNome(nome: String): Categoria =
            entries.firstOrNull { it.name == nome } ?: OUTROS
    }
}

data class Transacao(
    val id: Long,
    val titulo: String,
    val valor: Double,
    val tipo: TipoTransacao,
    val categoria: Categoria,
    /** Data em milissegundos (epoch). */
    val data: Long,
    /** Preenchido quando a transação é o pagamento de uma conta fixa. */
    val contaId: Long? = null,
    /** Preenchido quando a transação é um valor guardado numa meta. */
    val metaId: Long? = null
)

/** Conta que se repete todo mês (aluguel, internet, assinaturas...). */
data class ContaFixa(
    val id: Long,
    val nome: String,
    val valor: Double,
    val diaVencimento: Int,
    val categoria: Categoria
)

/** Objetivo de economia (viagem, reserva de emergência...). */
data class Meta(
    val id: Long,
    val nome: String,
    val emoji: String,
    val valorAlvo: Double
)
