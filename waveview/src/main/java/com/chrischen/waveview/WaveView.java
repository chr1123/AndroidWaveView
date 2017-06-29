package com.chrischen.waveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 2017/6/28.
 */

public class WaveView extends View {
    public WaveView(Context context) {
        super(context);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private Integer waveType;//波形展示类型
    private int centerLineColor = Color.BLACK;
    private int centerLineWidth = 1;
    private int lineColor = Color.GREEN;
    private int lineWidth = 10;//竖线的宽度
    private int lineSpace = 30;//竖线之间的间隔宽度

    public static final int WVTYPE_CENTER_LINE = 0;//竖线从中间开始 向上向下长度相同
    public static final int WVTYPE_SINGLE = 1;//竖线从底部开始向上计算


    private List<Integer> values;//存放数值
    private int fullValue = 100;//相对最大值
    private float mScale = 0;//传入值转换为有效数值需要使用的比例
    private int maxValue = 1;//当前数组中的最大值 该值乘以scale应等于fullValue
    private int maxLineCount ;
    private boolean hasOver;//值记录是否已完毕

    Paint paintCenterLine,paintLine;



    private void init(AttributeSet attrs){
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WaveView);
        waveType = typedArray.getInt(R.styleable.WaveView_wvType, 0);
        centerLineColor = typedArray.getColor(R.styleable.WaveView_wvCenterLineColor,Color.BLACK);
        centerLineWidth = typedArray.getDimensionPixelSize(R.styleable.WaveView_wvCenterLineWidth,1);
        lineColor = typedArray.getColor(R.styleable.WaveView_wvLineColor,Color.GREEN);
        lineWidth = typedArray.getDimensionPixelSize(R.styleable.WaveView_wvLineWidth,10);
        lineSpace = typedArray.getDimensionPixelSize(R.styleable.WaveView_wvLineSpace,30);

        paintCenterLine = new Paint();
        paintCenterLine.setStrokeWidth(centerLineWidth);
        paintCenterLine.setColor(centerLineColor);

        paintLine = new Paint();
        paintLine.setStrokeWidth(lineWidth);
        paintLine.setAntiAlias(true);
        paintLine.setColor(lineColor);
    }



    public void putValue(int value){
        if (value>maxValue){
            maxValue = value;
            mScale = (float) fullValue/maxValue;
        }
        if (values==null){
            values = new ArrayList<>();
        }else {
//            values.add(value);
//            invalidate();
        }
        values.add(value);
        invalidate();
    }

    public void setHasOver(boolean over){
        hasOver = over;
    }

    public boolean hasOver(){
        return hasOver;
    }


    private int lastX,moveX;
    private boolean hasBeenEnd=false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) (event.getRawX());
                break;
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getRawX();
                //到达边缘时不能向该方向继续移动
                if (!hasBeenEnd || (moveX>0&&(lastX-x)<0 ||(moveX<0&&(lastX-x)>0)))  {
                    moveX += (lastX-x)*0.7;
                    lastX = x;
                    invalidate();
                }
                break;
        }
        return true;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        int yCenter = getHeight() / 2;
        if (maxLineCount==0){
            maxLineCount = getWidth()/(lineSpace+lineWidth);
        }
        if (waveType==WVTYPE_CENTER_LINE){
            /***************画中线*****************/
            canvas.drawLine(0,yCenter,getWidth(),yCenter,paintCenterLine);
        }
        /***************画竖线*****************/
        //判断当前数组中的数据是否超出了可画竖线最大条数
        if(values!=null){
            /**找出当前第一条竖线以及偏移量*/
            int startIndex = 0;//第一条线
            int startOffset = 0;//第一条线的偏移
            if (!hasOver || moveX==0){//仍在记录中或未手动滑动过
                //线条数量超出最大数 只画后面的线
                if(values.size()>maxLineCount){
                    startIndex = values.size()-maxLineCount;
                }
            }else {//已结束录值 且x轴有过移动
                //先得到第一条线原本应该的位置
                if(values.size()>maxLineCount){
                    startIndex = values.size()-maxLineCount;
                }
                //计算移动线条数
                int moveLineSize = moveX/(lineWidth+lineSpace);
                startOffset = moveX%(lineWidth+lineSpace);
                int currentIndex = startIndex+moveLineSize;
                if (currentIndex<0){//到达最左边
                    startIndex = 0;
                    startOffset = 0;
                    hasBeenEnd = true;
                }else if (currentIndex>=values.size()){
                    startIndex = values.size()-1;
                    startOffset=0;
                    hasBeenEnd = true;
                }else {
                    startIndex = currentIndex;
                    hasBeenEnd = false;
                }
                Log.d("XXXXXXX","move-x:"+moveX+"   moveLineSize:"+moveLineSize
                        +"   startIndex:"+startIndex+"  startOffset:"+startOffset);
            }
            //画竖线
            for (int i=startIndex;i<values.size();i++){
                int startX =0;
                int endX =0;
                int startY =0;
                int endY =0;
                int lineHeight = (int) ((((float)values.get(i)*mScale)/fullValue)*getHeight());
                switch (waveType){
                    case WVTYPE_CENTER_LINE:
                        startX = (i-startIndex)*(lineSpace+lineWidth)+lineWidth/2 - startOffset ;
                        endX =  startX;
                        startY = (getHeight()-lineHeight)/2;
                        endY = (getHeight()-lineHeight)/2+lineHeight;
                        break;
                    case WVTYPE_SINGLE:
                        startX = (i-startIndex)*(lineSpace+lineWidth)+lineWidth/2 - startOffset ;
                        endX =  startX;
                        startY = getHeight()-lineHeight;
                        endY = getHeight();
                        break;
                }
                canvas.drawLine(startX,startY,endX,endY,paintLine);
               // Paint pNum = new Paint();
               // pNum.setColor(Color.RED);
               // canvas.drawText(""+i,startX,yCenter,pNum); 画出竖线index便于测试
            }



        }
    }


}
