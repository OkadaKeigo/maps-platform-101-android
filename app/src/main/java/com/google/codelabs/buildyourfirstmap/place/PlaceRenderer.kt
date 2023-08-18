package com.google.codelabs.buildyourfirstmap.place

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.codelabs.buildyourfirstmap.BitmapHelper
import com.google.codelabs.buildyourfirstmap.R
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * Place オブジェクト用のカスタム クラスター レンダラー
 */
class PlaceRenderer(
    private val context: Context, map: GoogleMap, clusterManager: ClusterManager<Place>
) : DefaultClusterRenderer<Place>(context, map, clusterManager) {

    /**
     * 各クラスタ項目に使用するアイコン
     */
    private val bicycleIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(
            context, R.color.colorPrimary
        )
        BitmapHelper.vectorToBitmap(
            context, R.drawable.ic_directions_bike_black_24dp, color
        )
    }

    /**
     * クラスターアイテム (マーカー) がレンダリングされる前に呼び出されるメソッド。
     * ここでマーカー オプションを設定する必要があります。
     */
    override fun onBeforeClusterItemRendered(
        item: Place, markerOptions: MarkerOptions
    ) {
        markerOptions.title(item.name).position(item.latLng).icon(bicycleIcon)
    }

    /**
     * クラスター アイテム (マーカー) がレンダリングされた直後に呼び出されるメソッド。
     * ここで、Marker オブジェクトのプロパティを設定する必要があります。
     */
    override fun onClusterItemRendered(clusterItem: Place, marker: Marker) {
        marker.tag = clusterItem
    }
}