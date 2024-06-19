package com.example.logicconditions

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.Drawable.createFromStream
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.logicconditions.Util.flipImageView
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

        val cvPlayer = listOf(
            cvPlayer1, cvPlayer2, cvPlayer3, cvPlayer4,
        )
        val playerImageViews = listOf(
            ivPlayer1Card1, ivPlayer2Card1, ivPlayer3Card1, ivPlayer4Card1,
            ivPlayer1Card2, ivPlayer2Card2, ivPlayer3Card2, ivPlayer4Card2,
            ivPlayer1Card3, ivPlayer2Card3, ivPlayer3Card3, ivPlayer4Card3,

        )

        val  playerStatus = arrayOf(
            binding.tvPlayer1Status,
            binding.tvPlayer2Status,
            binding.tvPlayer3Status,
            binding.tvPlayer4Status
        )

        val assetManager = assets
        val playingCardsDir = "playing_cards"

        btPlay.setOnClickListener {
            playerStatus.forEach { it.text = "" }
            cvPlayer.forEach { it.visibility = View.GONE }
            it.visibility = View.GONE
            ivCardDistribution.visibility = View.VISIBLE
            ivCardDistributionBackground.visibility = View.VISIBLE

            cardName = randomizeCard()
            var count = 0

            playerImageViews.forEach { it.setImageDrawable(null) }
            playerImageViews.forEach { it.foreground = null }


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

                                imageView.setImageDrawable(getDrawable(R.drawable.ic_deck_backside))
                                image.close()
                            }

                            override fun onAnimationRepeat(animation: Animation?) {}
                        })

                        ivCardDistribution.startAnimation(animation)
                        count++
                        if (count == 4) count = 0
                        delay(300)
                    }
                }
                determineWinner(cardName,playerStatus)

                ivCardDistribution.animation?.cancel()
                it.visibility = View.VISIBLE
                ivCardDistribution.visibility = View.GONE
                ivCardDistributionBackground.visibility = View.GONE
                cvPlayer.forEach {
                    it.visibility = View.VISIBLE
                }

            }

        }
        cvPlayer.forEachIndexed { index, playerView ->
            playerView.setOnClickListener {

                for (i in cardName.indices) {
                    val imageName = cardName[i]
                    val imageView = playerImageViews[i]
                    val image = assetManager.open("$playingCardsDir/$imageName")
                    val drawable = createFromStream(image, imageName)
                    imageView.setImageDrawable(drawable)
                    image.close()
                }
                cvPlayer.forEach {
                    it.visibility = View.GONE
                }
            }
        }
    }
    fun getAllPlayingCardImageNames(context: Context): List<String> {
        try {
            val assetManager = context.assets
            val playingCardsDir = "playing_cards"
            imageList = assetManager.list(playingCardsDir)!!.toList().shuffled() as ArrayList<String>
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

    private fun getCardValue(cardName: String): Int {
        return when {
            cardName.contains("ace", ignoreCase = true) -> 17
            cardName.contains("king", ignoreCase = true) -> 16
            cardName.contains("queen", ignoreCase = true) -> 15
            cardName.contains("jack", ignoreCase = true) -> 14
            cardName.contains("10") -> 10
            cardName.contains("9") -> 9
            cardName.contains("8") -> 8
            cardName.contains("7") -> 7
            cardName.contains("6") -> 6
            cardName.contains("5") -> 5
            cardName.contains("4") -> 4
            cardName.contains("3") -> 3
            cardName.contains("2") -> 2
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

    private fun isPureSequence(cards: List<String>): Boolean {
        val suits = cards.map { it.substringAfterLast("_of_") }.toSet()
        return suits.size == 1
    }

    private fun containsSequence(cards: List<String>, sequence: List<String>): Boolean {
        val cardValues = cards.map { getCardValue(it) }.sorted()
        val sequenceValues = sequence.map { getCardValue(it) }.sorted()
        return cardValues == sequenceValues
    }

    private fun isSameSuit(cards: List<String>): Boolean {
        val suits = cards.map { it.substringAfterLast("_of_") }.toSet()
        return suits.size == 1
    }

    private fun isDoublePair(cards: List<String>): Boolean {
        val valueCount = cards.groupingBy { getCardValue(it) }.eachCount()
        return valueCount.values.count { it == 2 } == 1
    }

    private fun checkSpecialCombinations(cards: List<String>): Int {
        if (isTrail(cards)) {
            return 120 // Highest priority
        }

        val sequences = listOf(
            listOf("queen", "king", "ace"),
            listOf("jack", "queen", "king"),
            listOf("10", "jack", "queen"),
            listOf("ace", "2", "3"),
            listOf("9", "10", "jack"),
            listOf("8", "9", "10"),
            listOf("7", "8", "9"),
            listOf("6", "7", "8"),
            listOf("5", "6", "7"),
            listOf("4", "5", "6"),
            listOf("3", "4", "5"),
            listOf("2", "3", "4")
        )

        // Function to check if two lists contain the same elements irrespective of order
        fun List<String>.containsSameElements(other: List<String>): Boolean {
            if (this.size != other.size) return false
            val thisSorted = this.sorted()
            val otherSorted = other.sorted()
            return thisSorted == otherSorted
        }

        if (isPureSequence(cards)) {
            for ((index, sequence) in sequences.withIndex()) {
                if (cards.containsSameElements(sequence)) {
                    return 90 + (sequences.size - index)
                }
            }
        }

        for ((index, sequence) in sequences.withIndex()) {
            if (cards.containsSameElements(sequence)) {
                return 70 + (sequences.size - index)
            }
        }

        if (isSameSuit(cards)) {
            return 50
        }

        if (isDoublePair(cards)) {
            return 40
        }

        return calculatePlayerScore(cards)
    }


    private fun getCardRank(cardName: String): Int {
        return when {
            cardName.contains("spades", ignoreCase = true) -> 4
            cardName.contains("hearts", ignoreCase = true) -> 3
            cardName.contains("diamonds", ignoreCase = true) -> 2
            cardName.contains("clubs", ignoreCase = true) -> 1
            else -> 0
        }
    }

    private fun determineHighestCard(cards: List<String>): String {
        return cards.maxWithOrNull(compareBy({ getCardValue(it) }, { getCardRank(it) })) ?: ""
    }

    private fun compareCards(cards1: List<String>, cards2: List<String>): Int {
        val sortedCards1 = cards1.sortedWith(compareBy({ getCardValue(it) }, { getCardRank(it) }))
        val sortedCards2 = cards2.sortedWith(compareBy({ getCardValue(it) }, { getCardRank(it) }))

        for (i in sortedCards1.indices) {
            val card1Value = getCardValue(sortedCards1[i])
            val card2Value = getCardValue(sortedCards2[i])
            if (card1Value != card2Value) {
                return card1Value - card2Value
            }

            val card1Rank = getCardRank(sortedCards1[i])
            val card2Rank = getCardRank(sortedCards2[i])
            if (card1Rank != card2Rank) {
                return card1Rank - card2Rank
            }
        }
        return 0
    }

    private fun isTrail(cards: List<String>): Boolean {
        return cards.groupingBy { getCardValue(it) }.eachCount().values.all { it == 3 }
    }

    private fun compareTrails(trail1: List<String>, trail2: List<String>): Int {
        val value1 = getCardValue(trail1.first())
        val value2 = getCardValue(trail2.first())
        return value1 - value2
    }

    private fun determineWinner(cardName: ArrayList<String>, playerStatus: Array<TextView>): Int {

        val sequences = listOf(
            listOf("queen", "king", "ace"),
            listOf("jack", "queen", "king"),
            listOf("10", "jack", "queen"),
            listOf("ace", "2", "3"),
            listOf("9", "10", "jack"),
            listOf("8", "9", "10"),
            listOf("7", "8", "9"),
            listOf("6", "7", "8"),
            listOf("5", "6", "7"),
            listOf("4", "5", "6"),
            listOf("3", "4", "5"),
            listOf("2", "3", "4")
        )

        val players = 4
        val scores = IntArray(players)
        val highestCards = Array(players) { "" }
        val playerCardsList = Array(players) { listOf<String>() }
        val isTrails = BooleanArray(players) { false }
        val isPureSequences = BooleanArray(players) { false }
        val isSequences = BooleanArray(players) { false }
        val isSameSuits = BooleanArray(players) { false }
        val isDoublePairs = BooleanArray(players) { false }

        for (player in 0 until players) {
            val playerCards = listOf(
                cardName[player],
                cardName[player + players],
                cardName[player + 2 * players]
            )
            scores[player] = checkSpecialCombinations(playerCards)
            highestCards[player] = determineHighestCard(playerCards)
            playerCardsList[player] = playerCards
            isTrails[player] = isTrail(playerCards)
            isPureSequences[player] = isPureSequence(playerCards)
            isSequences[player] = sequences.any { containsSequence(playerCards, it) }
            isSameSuits[player] = isSameSuit(playerCards)
            isDoublePairs[player] = isDoublePair(playerCards)
            Log.d("TAG", "Player ${player + 1}, Player cards [${playerCards}] wins with score: ${scores[player]}")
        }

        fun comparePlayers(player1: Int, player2: Int): Int {
            return when {
                isTrails[player1] && isTrails[player2] -> compareTrails(playerCardsList[player1], playerCardsList[player2])
                isTrails[player1] -> 1
                isTrails[player2] -> -1
                isPureSequences[player1] && isPureSequences[player2] -> compareCards(playerCardsList[player1], playerCardsList[player2])
                isPureSequences[player1] -> 1
                isPureSequences[player2] -> -1
                isSequences[player1] && isSequences[player2] -> compareCards(playerCardsList[player1], playerCardsList[player2])
                isSequences[player1] -> 1
                isSequences[player2] -> -1
                isSameSuits[player1] && isSameSuits[player2] -> compareCards(playerCardsList[player1], playerCardsList[player2])
                isSameSuits[player1] -> 1
                isSameSuits[player2] -> -1
                isDoublePairs[player1] && isDoublePairs[player2] -> compareCards(playerCardsList[player1], playerCardsList[player2])
                isDoublePairs[player1] -> 1
                isDoublePairs[player2] -> -1
                else -> compareCards(playerCardsList[player1], playerCardsList[player2])
            }
        }

        val maxScore = scores.maxOrNull()
        val winners = scores.indices.filter { scores[it] == maxScore }

        val winner = if (winners.size == 1) {
            winners[0]
        } else {
            winners.reduce { currentWinner, nextPlayer ->
                if (comparePlayers(currentWinner, nextPlayer) > 0) currentWinner else nextPlayer
            }
        }

        Toast.makeText(this, "Player ${winner + 1} wins with score: ${scores[winner]}", Toast.LENGTH_LONG).show()

        if (winner != -1) {
            playerStatus[winner].text = "Winner"
        } else {
            playerStatus[winner].text = ""
        }

        return winner
    }



    private fun noStatusBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

}