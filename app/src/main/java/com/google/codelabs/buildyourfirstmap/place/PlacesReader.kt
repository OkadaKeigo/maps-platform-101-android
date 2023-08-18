package com.google.codelabs.buildyourfirstmap.place

import android.content.Context
import com.google.codelabs.buildyourfirstmap.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.io.InputStreamReader

/**
 * ファイル places.json から場所 JSON オブジェクトのリストを読み取ります
 */
class PlacesReader(private val context: Context) {

    // JSON から Place オブジェクトへの変換を担当する GSON オブジェクト
    private val gson = Gson()

    // places.jsonを表すInputStream
    private val inputStream: InputStream
        get() = context.resources.openRawResource(R.raw.places)

    /**
     * ファイル places.json 内のプレイス JSON オブジェクトのリストを読み取ります
     * Place オブジェクトのリストを返します
     */
    fun read(): List<Place> {
        val itemType = object : TypeToken<List<PlaceResponse>>() {}.type
        val reader = InputStreamReader(inputStream)
        return gson.fromJson<List<PlaceResponse>>(reader, itemType).map {
            it.toPlace()
        }
    }
}