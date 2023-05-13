package com.example.post.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.post.dao.PostDao
import com.example.post.entities.Posts

//defines a Room Database annotation with three parameters
//entities: specifies the list of tables for the database
@Database(entities = [Posts::class], version = 1, exportSchema = false)

//defines an abstract class PostsDatabase that extends RoomDatabase.
abstract class PostsDatabase : RoomDatabase() {

    //define static members and methods
    companion object {
        var postsDatabase: PostsDatabase? = null

        //only one thread can access it at a time
        @Synchronized
        fun getDatabase(context: Context): PostsDatabase {
            if (postsDatabase == null) {
                postsDatabase = Room.databaseBuilder(
                    context
                    , PostsDatabase::class.java
                    , "posts.db"
                ).build()
            }
            //If the postsDatabase is not null, then it simply returns the existing instance.
            return postsDatabase!!
        }
    }

    //defines an abstract function postDao() that returns an instance of the PostDao interface.
    abstract fun postDao(): PostDao
}