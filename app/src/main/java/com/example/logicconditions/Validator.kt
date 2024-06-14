package com.example.logicconditions

import android.util.Log

class Validator {

    companion object {

        fun checkWinner(playerCards: List<String>) {

            for (player in playerCards) {
                Log.d("PlayerCards", "Player ${player + 1}: $playerCards")
            }


        }
    }


}