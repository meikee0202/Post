package com.example.post

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //replace the content of the activity with a HomeFragment(home,first activity)
        replaceFragment(HomeFragment.newInstance(),false)

    }

    //two parameters, a Fragment object and a boolean value istransition
    //fragment, new fragment that is to be displayed in place of the current fragment.
    //istransition, boolean flag which indicates whether to apply animation to the fragment transition or not.
    fun replaceFragment(fragment:Fragment, istransition:Boolean){

        //Creates a new FragmentTransaction object using the beginTransaction method of the supportFragmentManager object
        //supportFragmentManager.beginTransaction() creates a new FragmentTransaction object, which is used to add, replace, or remove fragments.
        val fragmentTransition = supportFragmentManager.beginTransaction()

        // If the istransition parameter is true, set custom animations for the fragment transition.
        if (istransition){
            fragmentTransition.setCustomAnimations(android.R.anim.slide_out_right,android.R.anim.slide_in_left)
        }
        //Replaces the content of the R.id.frame_layout view with the specified Fragment object, and adds the transaction to the back stack.
        fragmentTransition.add(R.id.frame_layout,fragment).addToBackStack(fragment.javaClass.simpleName).commit()
    }

    //called when the back button is pressed
    override fun onBackPressed() {

        // to perform the default behavior of the back button.
        super.onBackPressed()
        // Gets a list of all fragments currently added to the activity.
        val fragments = supportFragmentManager.fragments

        //If there are no fragments in the list, finish the activity.
        if (fragments.size == 0){
            finish()
        }
    }
}