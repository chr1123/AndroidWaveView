package com.chrischen.waveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
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
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private List<Integer> values;//存放数值
    private int fullValue = 100;//相对最大值
    private float mScale = 0;//传入值转换为有效数值需要使用的比例
    private int maxValue = 1;//当前数组中的最大值 该值乘以scale应等于fullValue
    private int spaceEachLine=50;//竖线之间的间隔宽度
    private int lineWidth = 10;//竖线的宽度
    private int maxLineCount ;

    private boolean hasOver;//值记录是否已完毕

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
            maxLineCount = getWidth()/(spaceEachLine+lineWidth);
        }
        /***************画中线*****************/
        Paint paintCenterLine = new Paint();
        paintCenterLine.setStrokeWidth(2);
        paintCenterLine.setColor(Color.BLACK);
        canvas.drawLine(0,yCenter,getWidth(),yCenter,paintCenterLine);
        /***************画竖线*****************/
        //判断当前数组中的数据是否超出了可画竖线最大条数
        if(values!=null){
            //竖线画笔
            Paint paintLine = new Paint();
            paintLine.setStrokeWidth(lineWidth);
            paintLine.setAntiAlias(true);
            paintLine.setColor(Color.GREEN);

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
                int moveLineSize = moveX/(lineWidth+spaceEachLine);
                startOffset = moveX%(lineWidth+spaceEachLine);
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
                int startX = (i-startIndex)*(spaceEachLine+lineWidth)+lineWidth/2 - startOffset ;
                int endX =  startX;
                int lineHeight = (int) ((((float)values.get(i)*mScale)/fullValue)*getHeight());
                int startY = (getHeight()-lineHeight)/2;
                int endY = (getHeight()-lineHeight)/2+lineHeight;
                canvas.drawLine(startX,startY,endX,endY,paintLine);
                Paint pNum = new Paint();
                pNum.setColor(Color.RED);
               // canvas.drawText(""+i,startX,yCenter,pNum); 画出竖线index便于测试
            }



        }
    }


}
