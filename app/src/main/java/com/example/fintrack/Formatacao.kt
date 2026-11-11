package com.example.fintrack

import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private val localeBR: Locale = Locale.forLanguageTag("pt-BR")
private val formatoMoeda: NumberFormat = NumberFormat.getCurrencyInstance(localeBR)

private val nomesMeses = listOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
)

private val nomesDiasSemana = listOf(
    "domingo", "segunda", "terça", "quarta", "quinta", "sexta", "sábado"
)

/** Formata um valor como "R$ 1.234,56". */
fun formatarMoeda(valor: Double): String =
    formatoMoeda.format(valor).replace(' ', ' ')

/** Mesmo que [formatarMoeda], mas troca o valor por bolinhas quando [oculto] é true. */
fun formatarMoeda(valor: Double, oculto: Boolean): String =
    if (oculto) "R$ ••••" else formatarMoeda(valor)

/**
 * Representa um mês como um número único (ano * 12 + mês), o que facilita
 * comparar meses e avançar/voltar (+1 / -1).
 */
fun chaveMes(millis: Long): Int {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    return c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH)
}

fun chaveMesAtual(): Int = chaveMes(System.currentTimeMillis())

fun nomeMes(chave: Int): String {
    val ano = chave / 12
    val mes = chave % 12
    return if (ano == chaveMesAtual() / 12) nomesMeses[mes] else "${nomesMeses[mes]} $ano"
}

fun diasNoMes(chave: Int): Int {
    val c = Calendar.getInstance().apply {
        clear()
        set(chave / 12, chave % 12, 1)
    }
    return c.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun diaDoMes(millis: Long): Int =
    Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.DAY_OF_MONTH)

/** Data (meio-dia, para evitar problemas de fuso) de um dia específico de um mês. */
fun dataNoMes(chave: Int, dia: Int): Long {
    val c = Calendar.getInstance().apply {
        clear()
        set(chave / 12, chave % 12, dia.coerceIn(1, diasNoMes(chave)), 12, 0)
    }
    return c.timeInMillis
}

fun inicioDoDia(millis: Long): Long {
    val c = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return c.timeInMillis
}

private const val UM_DIA = 24L * 60 * 60 * 1000

/** "Hoje", "Ontem" ou "terça, 4 de novembro". */
fun formatarDiaRelativo(millis: Long): String {
    val hoje = inicioDoDia(System.currentTimeMillis())
    val dia = inicioDoDia(millis)
    return when (dia) {
        hoje -> "Hoje"
        hoje - UM_DIA -> "Ontem"
        else -> {
            val c = Calendar.getInstance().apply { timeInMillis = millis }
            val semana = nomesDiasSemana[c.get(Calendar.DAY_OF_WEEK) - 1]
            val mes = nomesMeses[c.get(Calendar.MONTH)].lowercase(localeBR)
            "$semana, ${c.get(Calendar.DAY_OF_MONTH)} de $mes"
        }
    }
}

fun ontem(): Long = System.currentTimeMillis() - UM_DIA

fun iniciais(nome: String): String =
    nome.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

/**
 * Máscara de moeda estilo "caixa registradora": usa só os dígitos digitados,
 * os dois últimos viram centavos. "5" -> "0,05", "123456" -> "1.234,56".
 * Retorna "" quando não há valor, para o placeholder aparecer.
 */
fun mascaraMoeda(texto: String): String {
    val digitos = texto.filter { it.isDigit() }.trimStart('0').take(11)
    if (digitos.isEmpty()) return ""
    val completo = digitos.padStart(3, '0')
    val reais = completo.dropLast(2).reversed().chunked(3).joinToString(".").reversed()
    return "$reais,${completo.takeLast(2)}"
}

/** Converte o texto digitado ("1.234,56", "12,5", "12.50") em número. */
fun converterValor(texto: String): Double? {
    val limpo = texto.trim().replace("R$", "").replace(" ", "")
    if (limpo.isEmpty()) return null
    val normalizado = if (limpo.contains(',')) {
        limpo.replace(".", "").replace(',', '.')
    } else {
        limpo
    }
    return normalizado.toDoubleOrNull()
}
