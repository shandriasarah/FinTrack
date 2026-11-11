package com.example.fintrack

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Salva os dados do app no próprio celular (SharedPreferences em JSON).
 * Simples e sem bibliotecas extras; dá para trocar por Room no futuro.
 */
class Armazenamento(context: Context) {

    private val prefs = context.getSharedPreferences("fintrack_dados", Context.MODE_PRIVATE)

    var primeiraExecucao: Boolean
        get() = prefs.getBoolean("primeira_execucao", true)
        set(valor) = prefs.edit().putBoolean("primeira_execucao", valor).apply()

    var nomeUsuario: String
        get() = prefs.getString("nome_usuario", "Sarah") ?: "Sarah"
        set(valor) = prefs.edit().putString("nome_usuario", valor).apply()

    var percentualReserva: Int
        get() = prefs.getInt("percentual_reserva", 20)
        set(valor) = prefs.edit().putInt("percentual_reserva", valor).apply()

    var ocultarValores: Boolean
        get() = prefs.getBoolean("ocultar_valores", false)
        set(valor) = prefs.edit().putBoolean("ocultar_valores", valor).apply()

    // ---------- Transações ----------

    fun carregarTransacoes(): List<Transacao> = lerLista("transacoes") { o ->
        Transacao(
            id = o.getLong("id"),
            titulo = o.getString("titulo"),
            valor = o.getDouble("valor"),
            tipo = TipoTransacao.valueOf(o.getString("tipo")),
            categoria = Categoria.porNome(o.getString("categoria")),
            data = o.getLong("data"),
            contaId = if (o.has("contaId")) o.getLong("contaId") else null,
            metaId = if (o.has("metaId")) o.getLong("metaId") else null
        )
    }

    fun salvarTransacoes(lista: List<Transacao>) = escreverLista("transacoes", lista) { t ->
        JSONObject().apply {
            put("id", t.id)
            put("titulo", t.titulo)
            put("valor", t.valor)
            put("tipo", t.tipo.name)
            put("categoria", t.categoria.name)
            put("data", t.data)
            t.contaId?.let { put("contaId", it) }
            t.metaId?.let { put("metaId", it) }
        }
    }

    // ---------- Contas fixas ----------

    fun carregarContas(): List<ContaFixa> = lerLista("contas") { o ->
        ContaFixa(
            id = o.getLong("id"),
            nome = o.getString("nome"),
            valor = o.getDouble("valor"),
            diaVencimento = o.getInt("dia"),
            categoria = Categoria.porNome(o.getString("categoria"))
        )
    }

    fun salvarContas(lista: List<ContaFixa>) = escreverLista("contas", lista) { c ->
        JSONObject().apply {
            put("id", c.id)
            put("nome", c.nome)
            put("valor", c.valor)
            put("dia", c.diaVencimento)
            put("categoria", c.categoria.name)
        }
    }

    // ---------- Metas ----------

    fun carregarMetas(): List<Meta> = lerLista("metas") { o ->
        Meta(
            id = o.getLong("id"),
            nome = o.getString("nome"),
            emoji = o.getString("emoji"),
            valorAlvo = o.getDouble("alvo")
        )
    }

    fun salvarMetas(lista: List<Meta>) = escreverLista("metas", lista) { m ->
        JSONObject().apply {
            put("id", m.id)
            put("nome", m.nome)
            put("emoji", m.emoji)
            put("alvo", m.valorAlvo)
        }
    }

    fun apagarTudo() {
        prefs.edit()
            .remove("transacoes")
            .remove("contas")
            .remove("metas")
            .apply()
    }

    // ---------- Auxiliares ----------

    private fun <T> lerLista(chave: String, converter: (JSONObject) -> T): List<T> {
        val texto = prefs.getString(chave, null) ?: return emptyList()
        return try {
            val array = JSONArray(texto)
            (0 until array.length()).mapNotNull { i ->
                try {
                    converter(array.getJSONObject(i))
                } catch (e: Exception) {
                    null // ignora um item corrompido em vez de perder tudo
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun <T> escreverLista(chave: String, lista: List<T>, converter: (T) -> JSONObject) {
        val array = JSONArray()
        lista.forEach { array.put(converter(it)) }
        prefs.edit().putString(chave, array.toString()).apply()
    }
}
