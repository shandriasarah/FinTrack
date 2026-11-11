package com.example.fintrack

import androidx.annotation.DrawableRes

enum class Tela(val rotulo: String, @DrawableRes val icone: Int) {
    INICIO("Início", R.drawable.ic_inicio),
    EXTRATO("Extrato", R.drawable.ic_extrato),
    NOVA_TRANSACAO("Novo", R.drawable.ic_adicionar),
    PLANEJAR("Planejar", R.drawable.ic_planejar),
    PERFIL("Perfil", R.drawable.ic_perfil)
}
