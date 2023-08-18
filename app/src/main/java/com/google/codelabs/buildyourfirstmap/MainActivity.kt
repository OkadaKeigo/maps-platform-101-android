// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelabs.buildyourfirstmap

import MarkerInfoWindowAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLngBounds
import com.google.codelabs.buildyourfirstmap.place.Place
import com.google.codelabs.buildyourfirstmap.place.PlaceRenderer
import com.google.codelabs.buildyourfirstmap.place.PlacesReader
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ktx.addCircle
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad

class MainActivity : AppCompatActivity() {
    private val places: List<Place> by lazy {
        PlacesReader(this).read()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        lifecycleScope.launchWhenCreated {
            // 地図を取得する
            val googleMap = mapFragment.awaitMap()

            // マップの読み込みが完了するまで待ちます
            googleMap.awaitMapLoad()

            // すべての場所が地図に表示されていることを確認してください
            val bounds = LatLngBounds.builder()
            places.forEach { bounds.include(it.latLng) }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))

            addClusteredMarkers(googleMap)
        }
    }

    private var circle: Circle? = null

    /**
     * 指定された[item]の周囲に[circle]を追加します
     */
    private fun addCircle(googleMap: GoogleMap, item: Place) {
        circle?.remove()
        circle = googleMap.addCircle {
            center(item.latLng)
            radius(1000.0)
            fillColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimaryTranslucent))
            strokeColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
        }
    }

    /**
     * クラスタリングのサポートを使用してマップにマーカーを追加します。
     */
    private fun addClusteredMarkers(googleMap: GoogleMap) {
        // ClusterManager クラスを作成し、カスタム レンダラを設定します。
        val clusterManager = ClusterManager<Place>(this, googleMap)
        clusterManager.renderer = PlaceRenderer(
            this, googleMap, clusterManager
        )

        // カスタム情報ウィンドウ アダプターを設定します
        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))

        // 場所を ClusterManager に追加します。
        clusterManager.addItems(places)
        clusterManager.cluster()

        // ClusterManager を OnCameraIdleListener として設定します。
        // ズームインまたはズームアウトすると再クラスタリングが可能。
        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }

        // itemの周りに円を表示
        clusterManager.setOnClusterItemClickListener { item ->
            addCircle(googleMap, item)
            return@setOnClusterItemClickListener false
        }

        //
        // カメラ移動時の透明度の変更について
        //
        googleMap.setOnCameraMoveStartedListener {
            // カメラが動き始めたら、マーカーのアルファ値を半透明に変更します。
            clusterManager.markerCollection.markers.forEach { it.alpha = 0.3f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 0.3f }
        }

        googleMap.setOnCameraIdleListener {
            // カメラの動きが停止したら、アルファ値を不透明に戻します。
            clusterManager.markerCollection.markers.forEach { it.alpha = 1.0f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }

            // カメラの動きが停止したときに再クラスタリングを実行できるように、
            // カメラの動きが停止したときにclusterManager.onCameraIdle()を呼び出します。
            clusterManager.onCameraIdle()
        }
    }
}
