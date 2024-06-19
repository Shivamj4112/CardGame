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