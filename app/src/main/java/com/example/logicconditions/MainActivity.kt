package com.example.logicconditions

import android.content.Context
import android.graphics.drawable.Drawable.createFromStream
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.logicconditions.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var imageList: ArrayList<String> = ArrayList()
    private var cardName: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        noStatusBar()
        setContentView(binding.root)

        binding.apply {

            onPlayButton()
        }
    }

    private fun ActivityMainBinding.onPlayButton() {

        val assetManager = assets
        val playingCardsDir = "playing_cards"

        btPlay.setOnClickListener {
            it.visibility = View.GONE
            ivCardDistribution.visibility = View.VISIBLE
            ivCardDistributionBackground.visibility = View.VISIBLE

            cardName = randomizeCard()
            var count = 0

            val playerImageViews = listOf(
                ivPlayer1Card1, ivPlayer2Card1, ivPlayer3Card1, ivPlayer4Card1,
                ivPlayer1Card2, ivPlayer2Card2, ivPlayer3Card2, ivPlayer4Card2,
                ivPlayer1Card3, ivPlayer2Card3, ivPlayer3Card3, ivPlayer4Card3,

                )
            playerImageViews.forEach { it.setImageDrawable(null) }
            playerImageViews.forEach { it.foreground = null }

            logPlayerCards(cardName)

            GlobalScope.launch(Dispatchers.Main) {
                for (i in cardName.indices) {
                    val imageName = cardName[i]
                    val imageView = playerImageViews[i]

                    val image = withContext(Dispatchers.IO) {
                        assetManager.open("$playingCardsDir/$imageName")
                    }

                    if (count <= 3) {
                        val currentAnimation = Util.animations[count]
                        val animation =
                            AnimationUtils.loadAnimation(this@MainActivity, currentAnimation)
                        animation.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {}
                            override fun onAnimationEnd(animation: Animation?) {
                                imageView.setImageDrawable(createFromStream(image, imageName))
                                    imageView.setImageDrawable(getDrawable(R.drawable.ic_deck_backside))
                                image.close()
                            }
//
                            override fun onAnimationRepeat(animation: Animation?) {}
                        })

                        ivCardDistribution.startAnimation(animation)
                        count++
                        if (count == 4) count = 0
                        delay(300)
                    }
                }
                determineWinner(cardName)
                ivCardDistribution.animation?.cancel()
                it.visibility = View.VISIBLE
                ivCardDistribution.visibility = View.GONE
                ivCardDistributionBackground.visibility = View.GONE
            }


        }
    }

    fun getAllPlayingCardImageNames(context: Context): List<String> {
        try {
            val assetManager = context.assets
            val playingCardsDir = "playing_cards"
            imageList =
                assetManager.list(playingCardsDir)!!.toList().shuffled() as ArrayList<String>
            return imageList
        } catch (e: IOException) {
            e.printStackTrace()
            return emptyList()
        }
    }

    fun randomizeCard(): ArrayList<String> {

        var i = 0
        val players = 4
        val cards = 3
        var randomCardName = ArrayList<String>()

        val list = getAllPlayingCardImageNames(this)

        while (i < players * cards) {

            var randomList = list.random()

            if (!randomCardName.contains(randomList)) {
                randomCardName.add(randomList)
                i++
            }

        }

        return randomCardName

    }


    private fun logPlayerCards(cardName: ArrayList<String>) {
        val players = 4

        for (player in 0 until players) {
            val playerCards = listOf(
                cardName[player],
                cardName[player + players],
                cardName[player + 2 * players]
            )
        }
    }

    private fun getCardValue(cardName: String): Int {
        return when {
            cardName.contains("ace", ignoreCase = true) -> 140
            cardName.contains("king", ignoreCase = true) -> 130
            cardName.contains("queen", ignoreCase = true) -> 120
            cardName.contains("jack", ignoreCase = true) -> 110
            cardName.contains("10") -> 100
            cardName.contains("9") -> 90
            cardName.contains("8") -> 80
            cardName.contains("7") -> 70
            cardName.contains("6") -> 60
            cardName.contains("5") -> 50
            cardName.contains("4") -> 40
            cardName.contains("3") -> 30
            cardName.contains("2") -> 20
            else -> 0
        }
    }

    private fun calculatePlayerScore(cards: List<String>): Int {
        var score = 0
        for (card in cards) {
            score += getCardValue(card)
        }
        return score
    }

    private fun determineWinner(cardName: ArrayList<String>) {
        val players = 4
        val scores = IntArray(players)

        for (player in 0 until players) {
            val playerCards = listOf(
                cardName[player],
                cardName[player + players],
                cardName[player + 2 * players]
            )
            scores[player] = calculatePlayerScore(playerCards)
        }

        val winner = scores.indices.maxByOrNull { scores[it] } ?: -1
        Toast.makeText(this, "Player ${winner + 1} wins with score: ${scores[winner]}", Toast.LENGTH_LONG).show()
    }

    private fun noStatusBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

}