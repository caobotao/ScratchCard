package scratchcard.cbt.com.scratchcard.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import scratchcard.cbt.com.scratchcard.R;

/**
 * Created by caobotao on 15/12/7.
 */
public class Scratchcard extends View {
    private Paint mOutterPaint;//画笔
    private Path mPath;//绘制路径
    private Canvas mCanvas;//画布
    private Bitmap mBitmap;//图像

    private int mLastX;//画笔x轴位置
    private int mLastY;//画笔y轴位置
    private Bitmap mOutterBitmap;//刮刮卡覆盖层

//    private Bitmap bitmap;

    private String mText;//刮刮卡文本
    private Rect mTextBound;//刮刮卡的长和宽
    private Paint mBackPaint;//绘制刮刮卡文本的画笔
    private int mTextSize;//刮刮卡文本字体大小

    private volatile boolean mComplete = false;//判断遮盖层区域消除是否达到阈值

    //刮刮卡刮完的回调
    public interface OnScratchCardCompleteListener{
        void complete();
    }
    private OnScratchCardCompleteListener mListener;

    public void setOnScratchCardCompleteListener(OnScratchCardCompleteListener mListener) {
        this.mListener = mListener;
    }

    public Scratchcard(Context context) {
        this(context,null);
    }

    public Scratchcard(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public Scratchcard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        //初始化BitMap
        mBitmap = Bitmap.createBitmap(width,height, Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        //设置绘制path画笔的属性
        setUpOutterPaint();
        //设置刮刮卡信息的画笔属性
        setUpBackPaint();
//        mCanvas.drawColor(Color.parseColor(("#C0C0C0")));
        mCanvas.drawRoundRect(new RectF(0,0,width,height),30,30,mOutterPaint);
        mCanvas.drawBitmap(mOutterBitmap,null,new Rect(0,0,width,height),null);
    }

    //设置刮刮卡信息的画笔属性
    private void setUpBackPaint() {
        mBackPaint.setColor(Color.DKGRAY);
        mBackPaint.setStyle(Style.FILL);
        mBackPaint.setTextSize(mTextSize);
        //设置当前画笔绘制文本的宽和高
        mBackPaint.getTextBounds(mText,0,mText.length(),mTextBound);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mPath.moveTo(x,y);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - mLastX);
                int dy = Math.abs(y - mLastY);
                if (dx > 3 || dy > 3){
                    mPath.lineTo(x,y);
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                new Thread(mRunnable).start();
                break;
        }
        //刷新View
        invalidate();
        return true;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();
            float wipeArea = 0;
            float totalArea = w * h;
            int[] mPixels = new int[w * h];
            Bitmap bitmap = mBitmap;
            //获得mBitmap所有的像素信息
            bitmap.getPixels(mPixels,0,w,0,0,w,h);
            for (int i = 0; i < w; i ++){
                for (int j = 0; j < h; j ++){
                    int index = i + j * w;
                    if(mPixels[index] == 0){
                        wipeArea ++;
                    }
                }
            }
            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);
                Log.i("Tag",percent + "");
                if (percent > 60) {
                    //清除遮盖层区域
                    mComplete = true;
                    postInvalidate();

                }
            }
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawBitmap(bitmap,0,0,null);
        canvas.drawText(mText,getWidth()/2 - mTextBound.width()/2,
                getHeight()/2 + mTextBound.height()/2,mBackPaint);
        if (!mComplete){
            drawPath();
            canvas.drawBitmap(mBitmap,0,0,null);
        }
        else {
            if (mListener != null) {
                mListener.complete();
            }
        }

    }

    private void drawPath() {
        mOutterPaint.setStyle(Style.STROKE);//设置画笔的风格
        mOutterPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        mCanvas.drawPath(mPath,mOutterPaint);
    }

    //设置绘制path画笔的属性
    private void setUpOutterPaint() {
        mOutterPaint.setColor(Color.parseColor("#C0C0C0"));//画笔颜色
        mOutterPaint.setAntiAlias(true);//抗锯齿
        mOutterPaint.setDither(true);//防抖动
        mOutterPaint.setStrokeJoin(Join.ROUND);//设置画笔圆角
        mOutterPaint.setStrokeCap(Cap.ROUND);//设置画笔的图形样式
        mOutterPaint.setStyle(Style.FILL);//设置画笔的风格
        mOutterPaint.setStrokeWidth(20);//设置画笔的宽度
    }

    /*
        进行初始化操作
     */
    private void init() {
        mOutterPaint = new Paint();
        mBackPaint = new Paint();
        mPath = new Path();
        mOutterBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.fg_guaguaka);

//        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.t2);
        mText = "谢谢惠顾";
        mTextBound = new Rect();
        mTextSize = 30;
    }
}
