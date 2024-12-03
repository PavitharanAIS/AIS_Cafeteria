package com.examples.aiscafeteria

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.location.Address
import com.examples.aiscafeteria.model.UserModel
import android.util.Log


class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "users.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "users"
        private const val COLUMN_UID = "uid"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_IS_MODIFIED = "isModified"

    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE_QUERY = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_UID TEXT PRIMARY KEY," +
                "$COLUMN_EMAIL TEXT," +
                "$COLUMN_PASSWORD TEXT," +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_PHONE TEXT," +
                "$COLUMN_ADDRESS TEXT," +
                "$COLUMN_IS_MODIFIED INTEGER DEFAULT 0)")
        db.execSQL(CREATE_TABLE_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addUser(user: UserModel): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_UID, user.uid)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_NAME, user.name)
        }
        return db.insert(TABLE_NAME, null, contentValues)
    }

    fun updateUser(user: UserModel): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, user.name)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PHONE, user.phone)
            put(COLUMN_ADDRESS, user.address)
            put(COLUMN_IS_MODIFIED, 1) // Mark as modified

        }
        val rowsUpdated = db.update(TABLE_NAME, contentValues, "$COLUMN_UID = ?", arrayOf(user.uid))
        if (rowsUpdated == 0) {
            contentValues.put(COLUMN_UID, user.uid)
            db.insert(TABLE_NAME, null, contentValues)
        }
        return rowsUpdated
    }

    fun isUserModified(uid: String): Boolean {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME, arrayOf(COLUMN_IS_MODIFIED), "$COLUMN_UID = ?",
            arrayOf(uid), null, null, null
        )

        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLUMN_IS_MODIFIED)
            if (columnIndex != -1) {
                val isModified = cursor.getInt(columnIndex) == 1
                Log.d("isUserModified", "User modification status for UID $uid: $isModified")
                return isModified
            } else {
                Log.e("DatabaseError", "Column $COLUMN_IS_MODIFIED not found in the result set.")
                return false
            }
        }
        return false
    }



    fun markAsSynced(uid: String) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_IS_MODIFIED, 0)
        }
        db.update(TABLE_NAME, contentValues, "$COLUMN_UID = ?", arrayOf(uid))
    }



    fun getUserByUid(uid: String): UserModel? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME, null, "$COLUMN_UID = ?", arrayOf(uid),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            UserModel(
                uid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            )
        } else null
    }


    fun getUserByEmailAndPassword(email: String, password: String): UserModel? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME, null,
            "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(email, password), null, null, null
        )
        return if (cursor.moveToFirst()) {
            UserModel(
                uid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            )
        } else null
    }
}
