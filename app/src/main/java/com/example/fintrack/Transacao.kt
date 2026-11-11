package com.example.fintrack

import androidx.compose.ui.graphics.Color

enum class TipoTransacao {
    ENTRADA,
    SAIDA
}

enum class Categoria(val nome: String, val cor: Color, val sigla: String) {
    SALARIO("Salário", Color(0xFF2E7D32), "SAL"),
    MERCADO("Mercado", Color(0xFFD32F2F), "MER"),
    TRANSPORTE("Transporte", Color(0xFFD32F2F), "TRA"),
    FREELANCE("Freelance", Color(0xFF2E7D32), "FRE"),
    RESTAURANTE("Restaurante", Color(0xFFD32F2F), "RES"),
    FARMACIA("Farmácia", Color(0xFFD32F2F), "FAR"),
    ALUGUEL("Aluguel", Color(0xFFD32F2F), "ALU"),
    OUTROS("Outros", Color(0xFF757575), "OUT")
}

data class Transacao(
    val id: Int = 0,
    val titulo: String,
    val valor: Double,
    val tipo: TipoTransacao,
    val categoria: Categoria = Categoria.OUTROS,
    val dataGrupo: String = "Hoje"
)