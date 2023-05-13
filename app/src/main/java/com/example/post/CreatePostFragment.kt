package com.example.post

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.post.database.PostsDatabase
import com.example.post.entities.Posts
import com.example.post.util.PostBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*


//allows users to create and save posts
//uses the Android runtime permission system to request permission to read from the device's external storage
// includes functionality for selecting images from the device's storage and displaying them in the app



//inherit BaseFragment
//help with handling Android runtime permissions(easy permissions)
class CreatePostFragment : BaseFragment(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    var currentDate: String? = null
    //to represent a permission code for reading from external storage
    private var READ_STORAGE_PERM = 123
    //to represent a request code for launching an activity to select an image
    private var REQUEST_CODE_IMAGE = 456
    // to store the path of a selected image file in an Android application after the user selects an image from the device's storage
    private var selectedImagePath = ""
    private var postId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postId = requireArguments().getInt("postId", -1)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_post, container, false)
    }


    // to create a new instance of a fragment
    // newInstance() method can be used to pass arguments to the fragment using the arguments property.
    companion object {

        //to generate static Java methods for functions or properties
        @JvmStatic

        //create a new instance of the CreatePostFragment class.
        //can be called without having to instantiate the class first.
        fun newInstance() =
            CreatePostFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }


    //@RequiresApi , to indicate the minimum API level required for the annotated code to run.
    //Build.VERSION_CODES.O i, defined in the Build class that represents the API level of Android 8.0 (Oreo).
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (postId != -1) {

            //coroutine launched
            launch {
                context?.let {

                    //retrieves a specific post from the database using the getSpecificPost method of the postDao object.
                    var posts = PostsDatabase.getDatabase(it).postDao().getSpecificPost(postId)

                    //retrieved post's title and post text are then set to the etPostTitle and etPostDesc
                    etPostTitle.setText(posts.title)
                    etPostDesc.setText(posts.postText)


                    if (posts.imgPath != "") {
                        selectedImagePath = posts.imgPath!!
                        imgPost.setImageBitmap(BitmapFactory.decodeFile(posts.imgPath))
                        layoutImage.visibility = View.VISIBLE
                        imgPost.visibility = View.VISIBLE
                        imgDelete.visibility = View.VISIBLE
                    } else {
                        layoutImage.visibility = View.GONE
                        imgPost.visibility = View.GONE
                        imgDelete.visibility = View.GONE
                    }

                }
            }
        }

        //BroadcastReceiver object is registered to listen for broadcast messages with the action "bottom_sheet_action"
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(

            // The BroadcastReceiver's onReceive method will be called whenever a broadcast message with the specified action is received.
            BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )

        //get the date and time
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        currentDate = sdf.format(Date())
        tvDateTime.text = currentDate

        //the image click listener
        imgDone.setOnClickListener {
            if (postId != -1) {
                updatePost()
            } else {
                savePost()
            }
        }

        imgBack.setOnClickListener {
            // will pop the current fragment from the back stack and return to the previous fragment when clicked.
            requireActivity().supportFragmentManager.popBackStack()
        }

        imgMore.setOnClickListener {
            // will open a new instance of PostBottomSheetFragment and show it as a bottom sheet when clicked
            // The postId is passed to the bottom sheet fragment as an argument using newInstance(postId)
            var postBottomSheetFragment = PostBottomSheetFragment.newInstance(postId)
            postBottomSheetFragment.show(
                requireActivity().supportFragmentManager,
                "Post Bottom Sheet Fragment"
            )
        }

        imgDelete.setOnClickListener {

            //clear the selectedImagePath variable and hide the layoutImage when clicked.
            selectedImagePath = ""
            layoutImage.visibility = View.GONE

        }

    }

    private fun updatePost() {
        launch {

            //if the context object is not null and enters a block of code
            context?.let {
                //gets a specific post from the database using the postId parameter
                var posts = PostsDatabase.getDatabase(it).postDao().getSpecificPost(postId)

                posts.title = etPostTitle.text.toString()
                posts.postText = etPostDesc.text.toString()
                posts.dateTime = currentDate
                posts.imgPath = selectedImagePath

                // updates the post in the database with the modified properties.
                PostsDatabase.getDatabase(it).postDao().updatePost(posts)
                //clear the text
                etPostTitle.setText("")
                etPostDesc.setText("")

                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun savePost() {

        if (etPostTitle.text.isNullOrEmpty()) {
            Toast.makeText(context, "Post Title is Required", Toast.LENGTH_SHORT).show()
        } else if (etPostDesc.text.isNullOrEmpty()) {
            Toast.makeText(context, "Post Description is Required", Toast.LENGTH_SHORT).show()
        } else {

            launch {
                var posts = Posts()
                posts.title = etPostTitle.text.toString()
                posts.postText = etPostDesc.text.toString()
                posts.dateTime = currentDate
                posts.imgPath = selectedImagePath
                context?.let {
                    PostsDatabase.getDatabase(it).postDao().insertPosts(posts)
                    //clear text
                    etPostTitle.setText("")
                    etPostDesc.setText("")
                    layoutImage.visibility = View.GONE
                    imgPost.visibility = View.GONE
                    //go to previous activity
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }

    }

    private fun deletePost() {

        launch {
            //checks if the context is not null
            context?.let {
                //called to get an instance of the PostsDatabase class
                //postDao() method, to get an instance of the PostsDao interface.
                PostsDatabase.getDatabase(it).postDao().deleteSpecificPost(postId)
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }


    // to receive broadcasts from other components of the application.
    //object : BroadcastReceiver()  ,extends the BroadcastReceiver class and overrides its onReceive() method
    private val BroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        // called when a broadcast is received
        // two arguments: a Context object and an Intent object
        override fun onReceive(p0: Context?, p1: Intent?) {

            //gets the value of "action" extra from the Intent object and assigns it to a variable named action
            var action = p1!!.getStringExtra("action")

            when (action!!) {

                "Image" -> {
                    // read an image from the device's storage.
                    readStorageTask()
                }

                "DeletePost" -> {
                    //delete post
                    deletePost()
                }


                else -> {
                    layoutImage.visibility = View.GONE
                    imgPost.visibility = View.GONE
                }
            }
        }

    }

    //called when the fragment is being destroyed
    //ensure that any resources or listeners created by the fragment are properly cleaned up when the fragment is destroyed
    override fun onDestroy() {

        //this line is unregistering a broadcast receiver which was registered in the onCreateView method of the same fragment
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BroadcastReceiver)
        //performs any additional cleanup required by the framework
        super.onDestroy()

    }

    //indicating whether the app has the permission to read external storage or not
    private fun hasReadStoragePerm(): Boolean {

        //check whether the app has the specified permissions.
        return EasyPermissions.hasPermissions(
            //takes the current context of the fragment as a parameter to access the application's resources
            requireContext(),
            //specifies the permission to be checked, which is the read external storage permission
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }


    //checks if the app has permission to read the external storage of the device
    private fun readStorageTask() {
        if (hasReadStoragePerm()) {
            pickImageFromGallery()
        } else {
            EasyPermissions.requestPermissions(
                //request the permission.
                requireActivity(),
                getString(R.string.storage_permission_text),
                //identify the permission request in onRequestPermissionsResult()
                READ_STORAGE_PERM,
                //permission to read the external storage of the device.
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun pickImageFromGallery() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_IMAGE)
        }
    }


//handle the result of a permission request
    override fun onRequestPermissionsResult(
         //requestCode is the code passed to request permissions.
        requestCode: Int,
        //permissions is the array of permissions that were requested.
        permissions: Array<out String>,
        //grantResults is the results of the permission request
        // indicating whether the permissions were granted or denied.
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // handle the permission result and delegate to the EasyPermissions library.
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            requireActivity()
        )
    }


    //called when the user denies the requested permissions
    //requestCode: an integer code that identifies the request that was denied.
    //perms: a list of strings representing the permissions that were denied
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        //to check if any of the denied permissions were permanently denied by the user(DONT SHOW AGAIN)
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(), perms)) {
            //rompts the user to go to the app settings and grant the necessary permissions manually
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
    }

    //to access the features that require permission
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    //permission denied
    override fun onRationaleDenied(requestCode: Int) {

    }

    //permission accept
    override fun onRationaleAccepted(requestCode: Int) {

    }

}

