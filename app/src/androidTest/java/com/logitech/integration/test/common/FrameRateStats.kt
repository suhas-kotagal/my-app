package com.logitech.integration.test.common



/*
//TODO

 val frameMetrics = FrameMetricsAggregator()
        scenario.onActivity {
            println("onactivity")
            frameMetrics.add(it)
        }

        val frameData = frameMetrics.stop()

        var totalFrames = 0L
        var slowFrames = 0L
        var frozenFrames = 0L
        frameData!![FrameMetricsAggregator.TOTAL_INDEX].let { distributions ->
            totalFrames = distributions.size().toLong()

            for (i in 0 until distributions.size()) {
                val duration = distributions.keyAt(i)
                val frameCount = distributions.valueAt(i)

                if (duration > 16)  // takes more than 16ms: considered slow
                    slowFrames += frameCount
                if (duration > 700) // takes more than 700ms: considered frozen
                    frozenFrames += frameCount
            }
        }



        logger.info(">> totalFrames=$totalFrames, slowFrames=$slowFrames, frozenFrames=$frozenFrames" )


 */