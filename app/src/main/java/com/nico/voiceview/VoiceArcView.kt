package com.nico.voiceview

import android.content.Context
import android.graphics.*
import android.support.constraint.ConstraintLayout
import android.support.graphics.drawable.VectorDrawableCompat
import android.text.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import java.lang.Math.atan2
import java.util.*
import kotlin.math.roundToInt

class VoiceArcView : ViewGroup {
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    /**
     * 屏幕的中点坐标
     */
    private var hLine = 0
    private var vLine = 0

    private var mLeft = 0
    private var mTop = 0
    private var mBottom = 0
    private var mRight = 0
    private var mBackColor = 0

    /**
     * 真实底部框的path
     */
    private var mRealPath: Path? = null

    /**
     * 选中选项后底部框的path
     */
    private var mInnerPath: Path? = null

    /**
     * 用于绘制左右两个选项框角度的path
     */
    private var mPath: Path? = null

    /**
     * 右选项框的path
     */
    private var mRightPath: Path? = null

    /**
     * 左选项框的path
     */
    private var mLeftPath: Path? = null

    /**
     * 初始化的框
     */
    private var mRect: RectF? = null

    /**
     * 底部 初始化的框
     */
    private var mRealRect: RectF? = null

    /**
     * 底部 操作有目标对象的 框
     */
    private var mInnerRect: RectF? = null


    /**
     * 底部 操作有目标对象的 框
     */
    private var mLeftVoiceRect: RectF? = null

    /**
     * 底部 操作有目标对象的 框
     */
    private var mCenterVoiceRect: RectF? = null

    /**
     * 底部 操作有目标对象的 框
     */
    private var mRightVoiceRect: RectF? = null


    /**
     * 展示语音识别文本   框
     */
    private var mResultContentRect: RectF? = null

    private var mResultTextRect: RectF = RectF()

    /**
     * 发送按钮区域
     */
    private var mResultSendRect: Rect? = null

    /**
     * 取消按钮区域
     */
    private var mResultCancelRect: Rect? = null


    /**
     * 获取左右两个选项path结果的状态值
     */
    private var mLeftStatus = false
    private var mRightStatus = false

    /**
     * 左边选项 的 坐标以及角度
     */
    private var mLeftPosArray = floatArrayOf(0.0f, 0.0f)
    private var mLeftTanArray = floatArrayOf(0.0f, 0.0f)

    /**
     * 右边选项的坐标以及角度
     */
    private var mRightPosArray = floatArrayOf(0.0f, 0.0f)
    private var mRightTanArray = floatArrayOf(0.0f, 0.0f)

    /**
     * 0 初始
     * 1 左边
     * 2 右边
     * 3 中间
     * 4 展示结果
     */
//    private var mSelectStatus = 0;

    /**
     * 当前识别的字符
     */
    private var mCurrentString = ""

    /**
     * 页面当前状态
     */
    private var mCurrentStatus = VoiceviewStatus.INIT

    /**
     * 是否有内容
     */
    private var hasResultContent = false;

    /**
     * 圆环 初始状态和放大状态半径
     */
    private var cRadiusOrigin = 80.0f
    private var cRadiusScale = 100.0f

    private var leftMatrix: Matrix? = null
    private var rightMatrix: Matrix? = null


    //矩形波纹颜色
//    private val lineColor: Int = Color.parseColor("#00faf7")
//    private val LINE_W = 5//默认矩形波纹的宽度，9像素, 原则上从layout的attr获得

    //矩形波纹宽度
    private val lineWidth: Float = 5.0f
    private val textSize: Float = 40.0f
    private val DEFAULT_TEXT = ""
    private var voiceMessage = DEFAULT_TEXT
//    private val textColor: Int = Color.parseColor("#666666")
//    private var isStart = false

    private val MIN_WAVE_H = 3//最小的矩形线高，是线宽的2倍，线宽从lineWidth获得
    private val MAX_WAVE_H = 8//最高波峰，是线宽的4倍

    //默认矩形波纹的高度，总共10个矩形，左右各有10个
    private val DEFAULT_WAVE_HEIGHT = intArrayOf(3, 3, 3, 3, 3, 3, 3, 3, 3, 3)
//    private val mWaveList: LinkedList<Int> = LinkedList()

    private val rectRight = RectF()//右边波纹矩形的数据，10个矩形复用一个rectF
    private val rectLeft = RectF()//左边波纹矩形的数据

    var list: LinkedList<Int> = LinkedList()
//    private val UPDATE_INTERVAL_TIME = 100//100ms更新一次


    private lateinit var mVoiceOprCallback: VoiceOprCallback
    private lateinit var mVoiceViewCallback: VoiceViewCallback


    /**
     *  三角形箭头 的长度
     */
    private val mArrowTriangle = 20;

    /**
     * 结果 点击事件
     *  0 初始状态
     *  1 发送
     *  2 取消
     */
    private var resultTouchStatus = 0

    /**
     * 三个图标
     */
    private val bottomVoiceIcon =
        VectorDrawableCompat.create(context.resources, R.drawable.ic_baseline_volume_up_24, null);
    private val leftVoiceIcon =
        VectorDrawableCompat.create(context.resources, R.drawable.ic_baseline_close_24, null);
    private val rightVoiceIcon =
        VectorDrawableCompat.create(context.resources, R.drawable.ic_baseline_translate_24, null);

    private val resultSendEnableIcon =
        VectorDrawableCompat.create(context.resources, R.drawable.check_icon_green, null);
    private val resultSendUnableIcon =
        VectorDrawableCompat.create(context.resources, R.drawable.check_icon_gray, null);

    private val resultCancelIcon =
        VectorDrawableCompat.create(context.resources, R.drawable.back_icon_white, null);

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {

    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {

    }


    init {
        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        var dis = windowManager.defaultDisplay;
        var point: Point = Point()
        dis.getSize(point)
        screenWidth = point.x
        screenHeight = point.y


        hLine = screenWidth / 2
        vLine = screenHeight * 3 / 4;


        mLeft = -screenWidth / 2
        mRight = screenWidth * 3 / 2
        mTop = screenHeight * 3 / 4
        mBottom = (screenHeight * 3 / 4) + (2 * screenWidth)


        mRect =
            RectF(mLeft.toFloat(), mTop.toFloat() - 200, mRight.toFloat(), mBottom.toFloat() - 200)
        mRealRect =
            RectF(mLeft.toFloat(), mTop.toFloat() - 100, mRight.toFloat(), mBottom.toFloat() - 100)
        mInnerRect =
            RectF(mLeft.toFloat(), mTop.toFloat() - 80, mRight.toFloat(), mBottom.toFloat() - 80)

        mBackColor = Color.DKGRAY
        //mPath是为了绘制左右两个 圆选项准备的
        mPath = Path()
        var centerX = (screenWidth / 2).toFloat()
        var centerY = (screenWidth + (mTop)).toFloat() - 200
        mPath!!.addCircle(
            centerX, centerY,
            screenWidth.toFloat()
            , Path.Direction.CW
        )
//mRealPath 用于绘制初始化的底部圆弧
        mRealPath = Path()
        var realcenterX = (screenWidth / 2).toFloat()
        var realcenterY = (screenWidth + (mTop)).toFloat() - 50
        mRealPath!!.addCircle(
            realcenterX, realcenterY,
            screenWidth.toFloat()
            , Path.Direction.CW
        )


//mInnerPath 用于绘制操作后的底部圆弧
        mInnerPath = Path();
        var innerCenterX = (screenWidth / 2).toFloat()
        var innerCenterY = (screenWidth + (mTop)).toFloat() - 20
        mInnerPath!!.addCircle(
            innerCenterX, innerCenterY,
            screenWidth.toFloat()
            , Path.Direction.CW
        )



        mLeftPath = Path()
        mLeftPath!!.addArc(mRect, 250.0f, 10.0f)
        var leftpathMeasure: PathMeasure = PathMeasure()
        leftpathMeasure.setPath(mLeftPath, true)
        mLeftStatus = leftpathMeasure.getPosTan(0.0f, mLeftPosArray, mLeftTanArray)



        mRightPath = Path()
        mRightPath!!.arcTo(mRect, 290.0f, -10.0f)//得反向来绘制
        var rightpathMeasure: PathMeasure = PathMeasure()
        rightpathMeasure.setPath(mRightPath, true)
        mRightStatus = rightpathMeasure.getPosTan(0.0f, mRightPosArray, mRightTanArray)

        leftMatrix = initMatrix(mLeftPosArray, mLeftTanArray, true);
        rightMatrix = initMatrix(mRightPosArray, mRightTanArray, false);

        // 绘制左边 语音 框
        mLeftVoiceRect = RectF()
        var leftVoiceX = mLeftPosArray[0]
        var leftVoiceY = mLeftPosArray[1] /*-200*/

        mLeftVoiceRect!!.left = 50.0f
        mLeftVoiceRect!!.right = leftVoiceX + 100

        mLeftVoiceRect!!.top = leftVoiceY - 400
        mLeftVoiceRect!!.bottom = leftVoiceY - 250


        // 绘制 中间 语音框
        mCenterVoiceRect = RectF()
        var centerVoiceX = screenWidth / 2
        var centerVoiceY = mRightPosArray[1] /*-200*/
        mCenterVoiceRect!!.left = centerVoiceX - 150.0f
        mCenterVoiceRect!!.right = centerVoiceX + 150.0f

        mCenterVoiceRect!!.top = centerVoiceY - 400
        mCenterVoiceRect!!.bottom = centerVoiceY - 250


        //绘制 右边 语音框
        mRightVoiceRect = RectF()

        var rightVoiceX = mRightPosArray[0]
        var rightVoiceY = mRightPosArray[1] /*-200*/

        mRightVoiceRect!!.left = 150.0f
        mRightVoiceRect!!.right = screenWidth - 50.0f

        mRightVoiceRect!!.top = rightVoiceY - 400
        mRightVoiceRect!!.bottom = rightVoiceY - 250


        //绘制语音识别结果框
        mResultContentRect = RectF()

        mResultContentRect!!.left = 250.0f
        mResultContentRect!!.right = screenWidth - 50.0f

        mResultContentRect!!.top = rightVoiceY - 500
        mResultContentRect!!.bottom = rightVoiceY - 250

//绘制  取消按钮 框
        mResultCancelRect = Rect()
        var resultCancelX = mLeftPosArray[0].toInt()
        var resultCancelY = mLeftPosArray[1].toInt() /*-200*/
        mResultCancelRect!!.left = 50
        mResultCancelRect!!.right = resultCancelX + 100

        mResultCancelRect!!.top = resultCancelY /*- 400*/
        mResultCancelRect!!.bottom = resultCancelY/* - 250*/ + (cRadiusScale * 2).toInt()


        //绘制  发送按钮 框
        mResultSendRect = Rect()
        var resultSendX = mRightPosArray[0].toInt()
        var resultSendY = mRightPosArray[1].toInt() /*-200*/

        mResultSendRect!!.left = resultSendX - cRadiusScale.toInt()
        mResultSendRect!!.right = resultSendX + cRadiusScale.toInt()

        mResultSendRect!!.top = resultSendY - cRadiusScale.toInt()
        mResultSendRect!!.bottom = resultSendY + cRadiusScale.toInt()

        resetList(list, DEFAULT_WAVE_HEIGHT);

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        var paint = Paint();
//        Log.e(javaClass.simpleName, "onDraw  mSelectStatus=$mSelectStatus")
//        mSelectStatus
        when (mCurrentStatus) {
            VoiceviewStatus.INIT, VoiceviewStatus.LEFT, VoiceviewStatus.CENTER, VoiceviewStatus.RIGHT -> {
                updateLeftItem(canvas!!, paint)
                updateRightItem(canvas!!, paint)
                updateBottomLayout(canvas!!, paint)
                updateVoiceLayout(canvas!!, paint)
            }


            VoiceviewStatus.RESULT -> {
                updateResultLayout(canvas!!, paint)
            }
        }
//        if (mSelectStatus < 4) {
//            updateLeftItem(canvas!!, paint)
//            updateRightItem(canvas!!, paint)
//            updateBottomLayout(canvas!!, paint)
//            updateVoiceLayout(canvas!!, paint)
//        } else {
//            updateResultLayout(canvas!!, paint)
//        }
    }


    /**
     * 绘制箭头
     */
    private fun drawTrigAngle(xPos: Float, rectF: RectF, paint: Paint, canvas: Canvas) {
        //绘制底部三角
        var path = Path();
        path.moveTo(xPos - mArrowTriangle, rectF!!.bottom)
        path.lineTo(xPos + mArrowTriangle, rectF!!.bottom)
        path.lineTo(xPos, rectF!!.bottom + mArrowTriangle * 2)
        path.lineTo(xPos - 5, rectF!!.bottom + mArrowTriangle * 2)
        path.lineTo(xPos - mArrowTriangle, rectF!!.bottom);
        canvas.drawPath(path, paint)
    }

    /**
     * 更新结果布局
     */
    private fun updateResultLayout(canvas: Canvas, paint: Paint) {
        paint.isAntiAlias = true;
        var rightPosX = mRightPosArray[0]
        var rightPosY = mRightPosArray[1]

        var leftPosX = mLeftPosArray[0]
        var leftPosY = mLeftPosArray[1]

        drawResultTarget(canvas, paint)

        if (hasResultContent) {
            paint.color = Color.WHITE
            var circlePath = Path()
            circlePath.addCircle(rightPosX, rightPosY, cRadiusScale, Path.Direction.CW)
            canvas.drawPath(circlePath, paint)

            var cancelPath = Path()
            paint.color = Color.DKGRAY
            cancelPath.addCircle(leftPosX, leftPosY, cRadiusScale, Path.Direction.CW)
            canvas.drawPath(cancelPath, paint)

            canvas.save()
            var width = resultSendEnableIcon!!.intrinsicWidth
            var height = resultSendEnableIcon!!.intrinsicHeight
            resultSendEnableIcon!!.setBounds(0, 0, width, height);
            var rMatrix = initResultMatrix(mRightPosArray, mRightTanArray, false)
            canvas.setMatrix(rMatrix)
            resultSendEnableIcon.draw(canvas)
            canvas.restore()



            canvas.save()
            var cancelwidth = resultCancelIcon!!.intrinsicWidth
            var cancelheight = resultCancelIcon!!.intrinsicHeight
            resultCancelIcon!!.setBounds(0, 0, cancelwidth, cancelheight)
            var lMatrix = initResultMatrix(mLeftPosArray, mLeftTanArray, false)
            canvas.setMatrix(lMatrix)
            resultCancelIcon.draw(canvas)
            canvas.restore()


        } else {
            paint.color = Color.LTGRAY
            var circlePath = Path()
            circlePath.addCircle(rightPosX, rightPosY, cRadiusScale, Path.Direction.CW)
            canvas.drawPath(circlePath, paint)

            var cancelPath = Path()
            cancelPath.addCircle(leftPosX, leftPosY, cRadiusScale, Path.Direction.CW)
            paint.color = Color.DKGRAY
            canvas.drawPath(cancelPath, paint)

            canvas.save()

            var width = resultSendUnableIcon!!.intrinsicWidth
            var height = resultSendUnableIcon!!.intrinsicHeight
            resultSendUnableIcon!!.setBounds(0, 0, width, height);
            var rMatrix = initResultMatrix(mRightPosArray, mRightTanArray, false)
            canvas.setMatrix(rMatrix)
            resultSendUnableIcon!!.draw(canvas)
            canvas.restore()


            canvas.save()
            var cancelwidth = resultCancelIcon!!.intrinsicWidth
            var cancelheight = resultCancelIcon!!.intrinsicHeight
            resultCancelIcon!!.setBounds(0, 0, cancelwidth, cancelheight);
            var lMatrix = initResultMatrix(mLeftPosArray, mLeftTanArray, false)

            canvas.setMatrix(lMatrix)
            resultCancelIcon.draw(canvas)
            canvas.restore()
        }
    }

    private fun computeFontHeight(paint: Paint): Int {
        val fontMetrics: Paint.FontMetrics = paint.fontMetrics
        return (fontMetrics.bottom - fontMetrics.top).toInt()
    }


    private fun computeFontWidth(paint: Paint, str: String): Int {
        var iRet = 0
        if (str != null && str.isNotEmpty()) {
            val len: Int = str.length
            val widths = FloatArray(len)
            paint.getTextWidths(str, widths)
            for (j in 0 until len) {
                iRet += Math.ceil(widths[j].toDouble()).toInt()
            }
        }
        return iRet
    }


    /**
     * 更新底部布局
     */
    private fun updateBottomLayout(canvas: Canvas, paint: Paint) {
        paint.isAntiAlias = true

        when (mCurrentStatus) {
            VoiceviewStatus.LEFT -> {
                paint.color = Color.DKGRAY
                canvas!!.drawPath(mInnerPath!!, paint)
            }
            VoiceviewStatus.RIGHT -> {
                paint.color = Color.DKGRAY
                canvas!!.drawPath(mInnerPath!!, paint)
            }
            else -> {
                paint.color = Color.LTGRAY
                canvas!!.drawPath(mRealPath!!, paint)
                paint.color = Color.WHITE
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 10.0f
                canvas.drawPath(mRealPath!!, paint)
            }
        }

        when (mCurrentStatus) {
            VoiceviewStatus.INIT -> {
            }
            else -> {
                canvas.save()
                var width = bottomVoiceIcon!!.intrinsicWidth
                var height = bottomVoiceIcon!!.intrinsicHeight
                bottomVoiceIcon!!.setBounds(0, 0, width, height);
                canvas.translate(
                    (hLine - width / 2).toFloat(),
                    mInnerRect!!.top + screenHeight / 8
                );
                bottomVoiceIcon.draw(canvas);
                canvas.restore();
            }
        }
//        if (mSelectStatus == 1 || mSelectStatus == 2) {
//
//            paint.color = Color.DKGRAY
//            canvas!!.drawPath(mInnerPath!!, paint)
//        } else {
//            paint.color = Color.LTGRAY
//            canvas!!.drawPath(mRealPath!!, paint)
//
//
//            paint.color = Color.WHITE
//            paint.style = Paint.Style.STROKE
//            paint.strokeWidth = 10.0f
//            canvas.drawPath(mRealPath!!, paint)
//
//        }


//        if (mSelectStatus != 0) {
//
//            canvas.save()
//            var width = bottomVoiceIcon!!.intrinsicWidth
//            var height = bottomVoiceIcon!!.intrinsicHeight
//            bottomVoiceIcon!!.setBounds(0, 0, width, height);
//            canvas.translate(
//                (hLine - width / 2).toFloat(),
//                mInnerRect!!.top + screenHeight / 8
//            );
//            bottomVoiceIcon.draw(canvas);
//            canvas.restore();
//        }


    }

    /**
     * 更新右边的item项
     */
    private fun updateRightItem(canvas: Canvas, paint: Paint) {
        paint.reset();
        paint.isAntiAlias = true;
        var realPosX = mRightPosArray[0]
        var realPosY = mRightPosArray[1]
        if (mRightStatus) {
//            if (mSelectStatus == 2) {
//                paint.color = Color.GREEN
//                canvas.drawCircle(realPosX, realPosY, cRadiusScale, paint)
//            } else {
//                paint.color = Color.LTGRAY
//                canvas.drawCircle(realPosX, realPosY, cRadiusOrigin, paint)
//            }

            when (mCurrentStatus) {
                VoiceviewStatus.RIGHT -> {
                    paint.color = Color.GREEN
                    canvas.drawCircle(realPosX, realPosY, cRadiusScale, paint)
                }
                else -> {
                    paint.color = Color.LTGRAY
                    canvas.drawCircle(realPosX, realPosY, cRadiusOrigin, paint)
                }
            }
            canvas.save()
            var width = rightVoiceIcon!!.intrinsicWidth
            var height = rightVoiceIcon!!.intrinsicHeight
            rightVoiceIcon!!.setBounds(0, 0, width, height)
            canvas.setMatrix(rightMatrix)
            rightVoiceIcon.draw(canvas);
            canvas.restore();
        }
    }

    /**
     * 更新左边的item项
     */
    private fun updateLeftItem(canvas: Canvas, paint: Paint) {
        paint.reset()
        paint.isAntiAlias = true;
        var realPosX = mLeftPosArray[0]
        var realPosY = mLeftPosArray[1]
        if (mLeftStatus) {
//            if (mSelectStatus == 1) {
//                paint.color = Color.YELLOW
//                canvas.drawCircle(realPosX, realPosY, cRadiusScale, paint)
//            } else {
//                paint.color = Color.LTGRAY
//                canvas.drawCircle(realPosX, realPosY, cRadiusOrigin, paint)
//            }
            when (mCurrentStatus) {
                VoiceviewStatus.LEFT -> {
                    paint.color = Color.YELLOW
                    canvas.drawCircle(realPosX, realPosY, cRadiusScale, paint)
                }
                else -> {
                    paint.color = Color.LTGRAY
                    canvas.drawCircle(realPosX, realPosY, cRadiusOrigin, paint)
                }
            }
            canvas.save()
            var width = leftVoiceIcon!!.intrinsicWidth
            var height = leftVoiceIcon!!.intrinsicHeight
            leftVoiceIcon!!.setBounds(0, 0, width, height);
            canvas.setMatrix(leftMatrix)
            leftVoiceIcon.draw(canvas)
            canvas.restore()
        }
    }


    private fun updateVoiceLayout(canvas: Canvas, paint: Paint) {
        paint.reset()
//        when (mSelectStatus) {
//            1 -> {
//                //左边选中
//                paint.color = Color.YELLOW
//                canvas.drawRoundRect(mLeftVoiceRect!!, 10.0f, 10.0f, paint)
//                drawTrigAngle(mLeftPosArray[0], mLeftVoiceRect!!, paint, canvas)
//
//                var centerX = (mLeftVoiceRect!!.right + mLeftVoiceRect!!.left) / 2
//                var centerY = (mLeftVoiceRect!!.bottom + mLeftVoiceRect!!.top) / 2
//
//                drawWaveLine(centerX.toInt(), centerY.toInt(), canvas, paint)
//
//
//                paint.color = Color.LTGRAY
//                paint.textSize = 40.0f
//                canvas.drawText("松开取消", mLeftPosArray[0] - 80, mLeftPosArray[1] - 120, paint)
//
//            }
//            2 -> {
//                //右边选中
//                paint.color = Color.GREEN
//                canvas.drawRoundRect(mRightVoiceRect!!, 10.0f, 10.0f, paint)
//
//                drawTrigAngle(mRightPosArray[0], mRightVoiceRect!!, paint, canvas)
//
//                var centerX = (mRightVoiceRect!!.right + mRightVoiceRect!!.left) / 2
//                var centerY = (mRightVoiceRect!!.bottom + mRightVoiceRect!!.top) / 2
//                drawWaveLine(centerX.toInt(), centerY.toInt(), canvas, paint)
//
//                paint.color = Color.LTGRAY
//                paint.textSize = textSize
//                canvas.drawText("转文字", mRightPosArray[0] - 40, mRightPosArray[1] - 120, paint)
//            }
//            3 -> {
//                //中间选中
//
//                paint.color = Color.GREEN
//                canvas.drawRoundRect(mCenterVoiceRect!!, 10.0f, 10.0f, paint)
//                drawTrigAngle(hLine.toFloat(), mCenterVoiceRect!!, paint, canvas)
//
//                var centerX = (mCenterVoiceRect!!.right + mCenterVoiceRect!!.left) / 2
//                var centerY = (mCenterVoiceRect!!.bottom + mCenterVoiceRect!!.top) / 2
//                drawWaveLine(centerX.toInt(), centerY.toInt(), canvas, paint)
//            }
//            else -> {
//                invalidate()
//            }
//        }
        when (mCurrentStatus) {
            VoiceviewStatus.LEFT -> {
                //左边选中
                paint.color = Color.YELLOW
                canvas.drawRoundRect(mLeftVoiceRect!!, 10.0f, 10.0f, paint)
                drawTrigAngle(mLeftPosArray[0], mLeftVoiceRect!!, paint, canvas)

                var centerX = (mLeftVoiceRect!!.right + mLeftVoiceRect!!.left) / 2
                var centerY = (mLeftVoiceRect!!.bottom + mLeftVoiceRect!!.top) / 2

                drawWaveLine(centerX.toInt(), centerY.toInt(), canvas, paint)


                paint.color = Color.LTGRAY
                paint.textSize = 40.0f
                canvas.drawText("松开取消", mLeftPosArray[0] - 80, mLeftPosArray[1] - 120, paint)

            }
            VoiceviewStatus.RIGHT -> {
                //右边选中
//                paint.color = Color.GREEN
//                canvas.drawRoundRect(mRightVoiceRect!!, 10.0f, 10.0f, paint)

                drawRightRecoContent(canvas, paint);


//                drawTrigAngle(mRightPosArray[0], mRightVoiceRect!!, paint, canvas)


//                paint.color = Color.LTGRAY
//                paint.textSize = textSize
//                canvas.drawText("转文字", mRightPosArray[0] - 40, mRightPosArray[1] - 120, paint)
            }
            VoiceviewStatus.CENTER -> {
                //中间选中

                paint.color = Color.GREEN
                canvas.drawRoundRect(mCenterVoiceRect!!, 10.0f, 10.0f, paint)
                drawTrigAngle(hLine.toFloat(), mCenterVoiceRect!!, paint, canvas)

                var centerX = (mCenterVoiceRect!!.right + mCenterVoiceRect!!.left) / 2
                var centerY = (mCenterVoiceRect!!.bottom + mCenterVoiceRect!!.top) / 2
                drawWaveLine(centerX.toInt(), centerY.toInt(), canvas, paint)
            }
            else -> {
                invalidate()
            }
        }

    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var action = event!!.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
//                Log.e(javaClass.simpleName, "==view=ACTION_DOWN==mSelectStatus = $mSelectStatus");
//                if (mSelectStatus < 4) {
//                    return false
//                } else {
//                    if (inTarget(event!!, mResultSendRect!!)) {
//                        resultTouchStatus = 1;
//                    } else if (inTarget(event!!, mResultCancelRect!!)) {
//                        resultTouchStatus = 2;
//                    }
//                }


                when (mCurrentStatus) {
                    VoiceviewStatus.LEFT, VoiceviewStatus.RIGHT, VoiceviewStatus.INIT, VoiceviewStatus.CENTER -> {
                        return false
                    }
                    else -> {
                        if (inTarget(event!!, mResultSendRect!!)) {
                            resultTouchStatus = 1;
                        } else if (inTarget(event!!, mResultCancelRect!!)) {
                            resultTouchStatus = 2;
                        }
                    }
                }

            }
            MotionEvent.ACTION_UP -> {
//                if (mSelectStatus >= 4) {
//                    when (resultTouchStatus) {
//                        0 -> {
//                        }
//                        1 -> {
//                            if (inTarget(event, mResultSendRect!!)) {
//                                //发送
//                                Log.e(javaClass.simpleName, "===发送消息===")
//                                resultTouchStatus = 0
//                                mSelectStatus = 0
//                                visibility = GONE
//                                mVoiceViewCallback.sendAction(voiceMessage)
//                            }
//                        }
//                        2 -> {
//                            if (inTarget(event, mResultCancelRect!!)) {
//                                //发送
//                                Log.e(javaClass.simpleName, "===取消消息===")
//                                resultTouchStatus = 0
//                                mSelectStatus = 0
//                                visibility = GONE
//                                mVoiceViewCallback.cancelAction()
//                            }
//                        }
//                    }
//                }


                when (mCurrentStatus) {
                    VoiceviewStatus.RESULT -> {
                        when (resultTouchStatus) {
                            0 -> {
                            }
                            1 -> {
                                if (inTarget(event, mResultSendRect!!)) {
                                    //发送
                                    Log.e(javaClass.simpleName, "===发送消息===")
                                    resultTouchStatus = 0
                                    mCurrentStatus = VoiceviewStatus.INIT
                                    visibility = GONE
                                    mVoiceViewCallback.sendAction(voiceMessage)
                                }
                            }
                            2 -> {
                                if (inTarget(event, mResultCancelRect!!)) {
                                    //发送
                                    Log.e(javaClass.simpleName, "===取消消息===")
                                    resultTouchStatus = 0
                                    mCurrentStatus = VoiceviewStatus.INIT
                                    visibility = GONE
                                    mVoiceViewCallback.cancelAction()
                                }
                            }
                        }
                    }
                    else -> {
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_CANCEL -> mVoiceViewCallback.cancelAction()
        }
        return true
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childCount = childCount
        Log.e(javaClass.simpleName, "childCount  $childCount")
        for (i in 0 until childCount) {
            val view: View = getChildAt(i)

            var showMsg: String = ""

            showMsg = if (hasResultContent) {
                voiceMessage
            } else {
                "未识别到文字"
            }
            var paint = Paint()
            paint.textSize = textSize
            var fontHeight = computeFontHeight(paint);
            var fontWidth = computeFontWidth(paint, showMsg)
            var singleWidth = mResultContentRect!!.width() - 120
            var lines = ((fontWidth / singleWidth) + 1).toInt()
            var rectHeight = fontHeight * lines + 120


            //绘制结果框的宽高
            mResultTextRect.left = mResultContentRect!!.left
            mResultTextRect.right = mResultContentRect!!.right
            mResultTextRect.bottom = mResultContentRect!!.bottom
            mResultTextRect.top = mResultContentRect!!.bottom - rectHeight
            Log.e(javaClass.simpleName, "drawCustomText   mResultTextRect   $mResultTextRect")
            view.layout(
                (mResultTextRect.left + 60).toInt(), (mResultTextRect.top + 30).toInt(),
                (mResultTextRect.right - 60).toInt(),
                (mResultTextRect.bottom).toInt()
            );
            Log.e(javaClass.simpleName, "onLayout   mResultTextRect   $mResultTextRect")
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val count = childCount
        for (i in 0 until count) {
            //这个很重要，没有就不显示
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun responseTouchEvent(event: MotionEvent?) {
        var action = event!!.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mVoiceOprCallback!!.startRecord()
            }

            MotionEvent.ACTION_UP -> doUpAction(event)
            MotionEvent.ACTION_MOVE -> doMoveAction(event)
        }
    }


    private fun doMoveAction(event: MotionEvent?) {
        Log.e(javaClass.simpleName, "==view=ACTION_MOVE==");

        var xPos = event!!.rawX
        var yPos = event!!.rawY
        if (yPos > vLine) {
            if (xPos >= mRightPosArray[0]) {
//                mSelectStatus = 2
                mCurrentStatus = VoiceviewStatus.RIGHT
            } else {
//                mSelectStatus = 3
                mCurrentStatus = VoiceviewStatus.CENTER

            }
        } else {
            if (xPos >= hLine) {
//                mSelectStatus = 2
                mCurrentStatus = VoiceviewStatus.RIGHT

            } else {
//                mSelectStatus = 1
                mCurrentStatus = VoiceviewStatus.LEFT

            }
        }
        invalidate()
    }


    private fun doUpAction(event: MotionEvent?) {
        mVoiceOprCallback!!.stopRecord()
        recognizeStringBuilder.clear()
        when (mCurrentStatus) {
            VoiceviewStatus.LEFT, VoiceviewStatus.INIT -> {
                mVoiceViewCallback.cancelAction()
                return
            }
        }
        mCurrentStatus = VoiceviewStatus.RESULT
        invalidate()
    }


    /**
     * view 重置
     */
    fun doReset() {
        resultTouchStatus = 0
//        mSelectStatus = 0
        mCurrentStatus = VoiceviewStatus.INIT
        hasResultContent = false
        voiceMessage = ""
        mCurrentString = ""
        removeAllViews()
    }


    /**
     *
     */
    private fun initMatrix(posArray: FloatArray, tanArray: FloatArray, isLeft: Boolean): Matrix {

        var imageWidth = 0
        var imageHeight = 0
        var realPosX = posArray[0]
        var realPosY = posArray[1]

        var degreePlus: Int = 0
        degreePlus = if (isLeft) {
            imageWidth = leftVoiceIcon!!.intrinsicWidth
            imageHeight = leftVoiceIcon!!.intrinsicHeight
            0
        } else {
            imageWidth = rightVoiceIcon!!.intrinsicWidth
            imageHeight = rightVoiceIcon!!.intrinsicHeight
            180
        }
        var matrix = Matrix()
        val degrees = (atan2(
            tanArray[1].toDouble(),
            tanArray[0].toDouble()
        ) * 180.0 / Math.PI).toFloat()
        // 旋转图片
        matrix.postRotate(
            degrees + degreePlus,
            (imageWidth / 2).toFloat(),
            (imageHeight / 2).toFloat()
        )
        // 将图片中心绘制到当前点
        matrix.postTranslate(
            realPosX.toFloat() - (imageWidth / 2).toFloat(),
            realPosY.toFloat() - (imageHeight / 2).toFloat()
        )

        return matrix
    }


    /**
     *  发送，取消按钮位置
     */
    private fun initResultMatrix(
        posArray: FloatArray,
        tanArray: FloatArray,
        isLeft: Boolean
    ): Matrix {

        var imageWidth = 0
        var imageHeight = 0
        var realPosX = posArray[0]
        var realPosY = posArray[1]


//        var degreePlus: Int = 0
        /*degreePlus =*/ if (isLeft) {
            imageWidth = leftVoiceIcon!!.intrinsicWidth
            imageHeight = leftVoiceIcon!!.intrinsicHeight
//            0
        } else {
            imageWidth = rightVoiceIcon!!.intrinsicWidth
            imageHeight = rightVoiceIcon!!.intrinsicHeight
//            180
        }
        var matrix = Matrix()
        // 将图片中心绘制到当前点
        matrix.postTranslate(
            realPosX - (imageWidth / 2).toFloat(),
            realPosY - (imageHeight / 2).toFloat()
        )
        return matrix
    }

    /**
     * 刷新音量
     * @param volume
     */
    @Synchronized
    fun newRefreshElement(volume: Int) {
        var maxAmp = volume.toFloat() / 30
        if (maxAmp > 1.0) {
            maxAmp = 1.0f
        }
        val waveH = MIN_WAVE_H + (maxAmp * (MAX_WAVE_H - 3)).roundToInt()//wave 在 4 ~ 9 之间
        list.add(0, waveH)
        list.removeLast()
        postInvalidate()
    }

    private fun resetList(list: LinkedList<Int>, array: IntArray) {
        list.clear()
        for (i in array.indices) {
            list.add(array[i])
        }
    }

    /**
     * 绘制 声波 波纹
     */
    private fun drawWaveLine(widthCenter: Int, heightCenter: Int, canvas: Canvas, paint: Paint) {
        val textWidth = 0f
        paint.reset()

        //更新左右两边的波纹矩形
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.strokeWidth = lineWidth
        paint.isAntiAlias = true
        for (i in 0..9) {
            //右边矩形
            rectRight.left = widthCenter + 2 * i * lineWidth + textWidth / 2 + lineWidth
            rectRight.top = heightCenter - list[i] * lineWidth / 2
            rectRight.right =
                widthCenter + 2 * i * lineWidth + 2 * lineWidth + textWidth / 2 /*- 10*/
            rectRight.bottom = heightCenter + list[i] * lineWidth / 2


            //左边矩形
            rectLeft.left = widthCenter - (2 * i * lineWidth + textWidth / 2 + 2 * lineWidth)
            rectLeft.top = heightCenter - list[i] * lineWidth / 2
            rectLeft.right = widthCenter - (2 * i * lineWidth + textWidth / 2 + lineWidth) /*- 10*/
            rectLeft.bottom = heightCenter + list[i] * lineWidth / 2

            canvas.drawRoundRect(rectRight, 5.0f, 5.0f, paint)
            canvas.drawRoundRect(rectLeft, 5.0f, 5.0f, paint)
        }
    }


    fun setOprCallback(callback: VoiceOprCallback) {
        mVoiceOprCallback = callback
    }

    fun setViewCallback(callback: VoiceViewCallback) {
        mVoiceViewCallback = callback
    }


    /**
     * 判断点击是否在对应的区域
     */
    private fun inTarget(event: MotionEvent, rect: Rect): Boolean {

        var targetRect = Rect()
        targetRect.left = rect.left - 25
        targetRect.right = rect.right + 25
        targetRect.top = rect.top - 25
        targetRect.bottom = rect.bottom + 25

        return targetRect.contains(event.rawX.toInt(), event.rawY.toInt())
    }


    /**
     * 绘制 结果 框
     */
    private fun drawResultTarget(canvas: Canvas, paint: Paint) {


        if (hasResultContent) {
            //右边选中
            paint.color = Color.GREEN
        } else {
            paint.color = Color.YELLOW
        }
        var showMsg: String = ""

        showMsg = if (hasResultContent) {
            voiceMessage
        } else {
            "未识别到文字"
        }
        paint.textSize = textSize
        var fontHeight = computeFontHeight(paint);
        var fontWidth = computeFontWidth(paint, showMsg)
        var singleWidth = mResultContentRect!!.width() - 120

        var lines = ((fontWidth / singleWidth) + 1).toInt()
        var rectHeight = fontHeight * lines + 120


        //绘制结果框的宽高
        mResultTextRect.left = mResultContentRect!!.left
        mResultTextRect.right = mResultContentRect!!.right
        mResultTextRect.bottom = mResultContentRect!!.bottom
        mResultTextRect.top = mResultContentRect!!.bottom - rectHeight


        Log.e(javaClass.simpleName, "drawCustomText   mResultTextRect   $mResultTextRect")

        canvas.drawRoundRect(mResultTextRect!!, 10.0f, 10.0f, paint)
        drawTrigAngle(mRightPosArray[0], mResultTextRect!!, paint, canvas)

        val textPaint = TextPaint()
        textPaint.isAntiAlias = true
        if (!hasResultContent) {
            textPaint.setARGB(0xFF, 0xFF, 0x00, 0x00)
            textPaint.textSize = textSize
            val layout = StaticLayout(
                showMsg,
                textPaint!!,
                singleWidth.toInt(),
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                true
            )
            canvas.save()
            canvas.translate(mResultTextRect.left + 60, mResultTextRect.top + 60)
            layout.draw(canvas)
            canvas.restore()
        }
    }


    fun showResult(voiceMessage: String) {
        hasResultContent = if (TextUtils.isEmpty(voiceMessage)) {
            showNoContentView()
            false
        } else {
            this.voiceMessage = voiceMessage
            showEditTextView(voiceMessage)
            true
        }
    }

    private fun showNoContentView() {
        //do nothing
    }


    /**
     * 在 输入框 中 展示 结果
     */
    private fun showEditTextView(str: String) {
        val editText = EditText(context)
        editText.setBackgroundColor(Color.parseColor("#00000000"))
        editText.setText(str)
        editText.setTextColor(Color.BLACK)
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        editText.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                voiceMessage = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        addView(
            editText,
            (mResultContentRect!!.width() - 120).toInt(),
            (mResultContentRect!!.height() - 30).toInt()
        )
    }


    fun updateRecognizeText(str: String) {
        mCurrentString = str
        postInvalidate()
    }


    /**
     * 绘制识别时候 右边 文本框， 内容
     */
    private fun drawRightRecoContent(canvas: Canvas, paint: Paint) {

        paint.reset()
        var currentString = ""
        paint.textSize = textSize
        var fontHeight = computeFontHeight(paint);
        var fontWidth = computeFontWidth(paint, currentString)
        var singleWidth = mRightVoiceRect!!.width() - 120  //框的宽度

        var lines = ((fontWidth / singleWidth) + 1).toInt()

        var rectHeight = fontHeight * lines + 120  //框的高度


        paint.color = Color.GREEN
        paint.isAntiAlias = true
        //绘制结果框的宽高
        var currentTextRectF = RectF()
        currentTextRectF.left = mRightVoiceRect!!.left
        currentTextRectF.right = mRightVoiceRect!!.right
        currentTextRectF.bottom = mRightVoiceRect!!.bottom
        currentTextRectF.top = mRightVoiceRect!!.bottom - rectHeight
        Log.e(javaClass.simpleName, "drawCustomText   mResultTextRect   $mResultTextRect")
        canvas.drawRoundRect(currentTextRectF!!, 10.0f, 10.0f, paint)
        drawTrigAngle(mRightPosArray[0], currentTextRectF, paint, canvas)

//        val textPaint = TextPaint()
//        textPaint.isAntiAlias = true
//        textPaint.setARGB(0xFF, 0xFF, 0x00, 0x00)
//        textPaint.color = Color.WHITE
//        textPaint.textSize = textSize
//        val layout = StaticLayout(
//            mCurrentString,
//            textPaint!!,
//            singleWidth.toInt(),
//            Layout.Alignment.ALIGN_NORMAL,
//            1.0f,
//            0.0f,
//            true
//        )
//        canvas.save()
//        canvas.translate(currentTextRectF.left + 60, currentTextRectF.top + 60)
//        layout.draw(canvas)
//        canvas.restore()


        drawMutipleText(
            canvas,
            currentTextRectF,
            singleWidth.toInt(),
            loadRecognizeText(mCurrentString),
            Color.BLACK
        )
        paint.reset()
        // 正在识音时候，音量条需要挪到右下角
        var rightBottomRectF = RectF()
        rightBottomRectF.left = mRightVoiceRect!!.right - 300
        rightBottomRectF.right = mRightVoiceRect!!.right
        rightBottomRectF.top = mRightVoiceRect!!.bottom - 100
        rightBottomRectF.bottom = mRightVoiceRect!!.bottom
        var centerX = (rightBottomRectF!!.right + rightBottomRectF!!.left) / 2
        var centerY = (rightBottomRectF!!.bottom + rightBottomRectF!!.top) / 2
        drawWaveLine(centerX.toInt(), centerY.toInt(), canvas, paint)

        paint.reset()
        paint.color = Color.LTGRAY
        paint.textSize = textSize
        canvas.drawText("转文字", mRightPosArray[0] - 40, mRightPosArray[1] - 120, paint)

    }


    //绘制多行文本
    private fun drawMutipleText(
        canvas: Canvas,
        rectF: RectF,
        singleWidth: Int,
        message: String,
        textColor: Int
    ) {
        val textPaint = TextPaint()
        textPaint.isAntiAlias = true
//        textPaint.setARGB(0xFF, 0xFF, 0x00, 0x00)
        textPaint.color = textColor
        textPaint.textSize = textSize
        val layout = StaticLayout(
            message,
            textPaint!!,
            singleWidth.toInt(),
            Layout.Alignment.ALIGN_NORMAL,
            1.0f,
            0.0f,
            true
        )
        canvas.save()
        canvas.translate(rectF.left + 60, rectF.top + 60)
        layout.draw(canvas)
        canvas.restore()
    }

    private var recognizeStringBuilder: StringBuilder = StringBuilder()
    private var loadDots = 0


    private var lastLoadTime = 0L

    private var refreshPeroid = 1000L

    /**
     * 识别语音的效果
     */
    private fun loadRecognizeText(message: String): String {
        if (System.currentTimeMillis() - lastLoadTime > refreshPeroid) {
            lastLoadTime = System.currentTimeMillis()
            recognizeStringBuilder.clear()
            recognizeStringBuilder.append(message)
            loadDots += 1;
            var time = loadDots % 3
            for (i in 0..time) {
                recognizeStringBuilder.append(".")
            }
        }
        return recognizeStringBuilder.toString();

    }
}
