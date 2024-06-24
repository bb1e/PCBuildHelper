package com.pcbuildhelper

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private var _threshold: Float =
        ObjectDetectorHelper.THRESHOLD_DEFAULT
    private var _maxResults: Int =
        ObjectDetectorHelper.MAX_RESULTS_DEFAULT

    val currentThreshold: Float get() = _threshold
    val currentMaxResults: Int get() = _maxResults

    fun setThreshold(threshold: Float) {
        _threshold = threshold
    }

    fun setMaxResults(maxResults: Int) {
        _maxResults = maxResults
    }
}
