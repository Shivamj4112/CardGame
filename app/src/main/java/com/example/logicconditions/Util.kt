package com.example.logicconditions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import java.io.IOException
import java.io.InputStream

object Util {

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
        listOf("2", "3", "4"),
        listOf("ace", "queen", "king"),
        listOf("king", "queen", "jack"),
        listOf("queen", "jack", "10"),
        listOf("3", "2", "ace"),
        listOf("jack", "10", "9"),
        listOf("10", "9", "8"),
        listOf("9", "8", "7"),
        listOf("8", "7", "6"),
        listOf("7", "6", "5"),
        listOf("6", "5", "4"),
        listOf("5", "4", "3"),
        listOf("4", "3", "2"),
        listOf("king", "queen", "ace"),
        listOf("queen", "jack", "king"),
        listOf("jack", "10", "queen"),
        listOf("2", "ace", "3"),
        listOf("10", "9", "jack"),
        listOf("9", "8", "10"),
        listOf("8", "7", "9"),
        listOf("7", "6", "8"),
        listOf("6", "5", "7"),
        listOf("5", "4", "6"),
        listOf("4", "3", "5"),
        listOf("3", "2", "4"),
        listOf("king", "ace", "queen"),
        listOf("queen", "king", "jack"),
        listOf("jack", "queen", "10"),
        listOf("2", "3", "ace"),
        listOf("10", "jack", "9"),
        listOf("9", "10", "8"),
        listOf("8", "9", "7"),
        listOf("7", "8", "6"),
        listOf("6", "7", "5"),
        listOf("5", "6", "4"),
        listOf("4", "5", "3"),
        listOf("3", "4", "2"),
        listOf("ace", "king", "queen"),
        listOf("king", "queen", "jack"),
        listOf("queen", "jack", "10"),
        listOf("3", "2", "ace"),
        listOf("jack", "10", "9"),
        listOf("10", "9", "8"),
        listOf("9", "8", "7"),
        listOf("8", "7", "6"),
        listOf("7", "6", "5"),
        listOf("6", "5", "4"),
        listOf("5", "4", "3"),)

     val animations = listOf(
        R.anim.card_distribution_player1,
        R.anim.card_distribution_player2,
        R.anim.card_distribution_player3,
        R.anim.card_distribution_player4,
    )


    fun flipImageView(imageView: ImageView, image: InputStream, imageName: String) {
        val scale = imageView.context.resources.displayMetrics.density
        val cameraDistance = 8000 * scale
        imageView.cameraDistance = cameraDistance

        val flipOut = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 90f)
        flipOut.duration = 500

        val flipIn = ObjectAnimator.ofFloat(imageView, "rotationY", -90f, 0f)
        flipIn.duration = 500

        flipOut.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)

                imageView.setImageDrawable(Drawable.createFromStream(image, imageName))
                image.close()
                flipIn.start()

            }

        })
        flipOut.start()
    }



}