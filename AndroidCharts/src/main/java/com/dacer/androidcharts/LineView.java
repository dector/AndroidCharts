package com.dacer.androidcharts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by Dacer on 11/4/13.
 * Edited by Lee youngchan 21/1/14
 * Edited by dector 30-Jun-2014
 */
// TODO implement instance state saving
public class LineView extends View {

    public enum PopupsVisibilityMode {
        NONE, MIN_MAX, ALL;

        @Deprecated
        static PopupsVisibilityMode fromOldValue(int value) {
            switch (value) {
                case SHOW_POPUPS_NONE:
                    return NONE;
                case SHOW_POPUPS_MAXMIN_ONLY:
                    return MIN_MAX;
                case SHOW_POPUPS_All:
                    return ALL;
            }

            return NONE;
        }

        public static PopupsVisibilityMode byIndex(int index) {
            return values()[index];
        }
    }

    private int mViewHeight;
    private boolean autoSetDataOfGird = true;
    private boolean autoSetGridWidth = true;
    private int dataOfAGird = 10;
    private int bottomTextHeight = 0;
    private ArrayList<String> bottomTextList;
    
    private ArrayList<ArrayList<Integer>> dataLists;

    private ArrayList<Integer> xCoordinateList = new ArrayList<Integer>();
    private ArrayList<Integer> yCoordinateList = new ArrayList<Integer>();
    
    private ArrayList<ArrayList<Dot>> drawDotLists = new ArrayList<ArrayList<Dot>>();

    private int bottomTextDescent;

    private final Paint backgroundLinesVerticalPaint = new Paint();
    private final Paint backgroundLinesHorizontalPaint = new Paint();
    private final Paint bottomTextPaint = new Paint();
    private final Paint popupTextPaint = new Paint();
    private final Paint dotOuterPaint = new Paint();
    private final Paint dotInnerPaint = new Paint();
    private final Paint linesPaint = new Paint();

    private final PopupPool popupPool;

    //popup
    private final int bottomTriangleHeight = 12;

	private Dot pointToSelect;
	private Dot selectedDot;

    private int topLineLength = MyUtils.dip2px(getContext(), 12);; // | | ←this
                                                                   //-+-+-
    private int sideLineLength = MyUtils.dip2px(getContext(),45)/3*2;// --+--+--+--+--+--+--
                                                                     //  ↑this
    private int backgroundGridWidth = MyUtils.dip2px(getContext(),45);

    //Constants
    private final int popupTopPadding = MyUtils.dip2px(getContext(), 2);
    private final int popupBottomMargin = MyUtils.dip2px(getContext(), 5);
    private final int bottomTextTopMargin = MyUtils.sp2px(getContext(), 5);
    private final int bottomLineLength = MyUtils.sp2px(getContext(), 22);
    private final int MIN_TOP_LINE_LENGTH = MyUtils.dip2px(getContext(),12);
    private final int MIN_VERTICAL_GRID_NUM = 4;
    private final int MIN_HORIZONTAL_GRID_NUM = 1;

    /**
     * @deprecated Use {@link com.dacer.androidcharts.LineView.PopupsVisibilityMode#NONE} instead
     */
    @Deprecated public static final int SHOW_POPUPS_All = 1;
    /**
     * @deprecated Use {@link com.dacer.androidcharts.LineView.PopupsVisibilityMode#MIN_MAX} instead
     */
    @Deprecated public static final int SHOW_POPUPS_MAXMIN_ONLY = 2;
    /**
     * @deprecated Use {@link com.dacer.androidcharts.LineView.PopupsVisibilityMode#ALL} instead
     */
    @Deprecated public static final int SHOW_POPUPS_NONE = 3;

    @Deprecated
    /**
     * @deprecated Use {@link #setPopupsVisibilityMode(com.dacer.androidcharts.LineView.PopupsVisibilityMode)} instead
     */
    public void setShowPopup(int popupType) {
        setPopupsVisibilityMode(PopupsVisibilityMode.fromOldValue(popupType));
	}

    private boolean drawDotLine;
    private boolean showPopupOnTouch;

    private int[] lineColors = { 0xffe74c3c, 0xff2980b9, 0xff1abc9c };
    private int[] popupColors = { 0xffe74c3c, 0xff2980b9, 0xff1abc9c };


    private PopupsVisibilityMode popupsVisibilityMode;

    // onDraw optimisations
    private final Point tmpPoint = new Point();
    private final Rect tmpRect = new Rect();
    private final Path tmpPath = new Path();

    // Attribute constants
    private static final int DEFAULT_BACKGROUND_COLOR = 0x00ffffff;
    private static final int DEFAULT_GRID_VERTICAL_LINE_THICKNESS_DIP = 1;
    private static final int DEFAULT_GRID_VERTICAL_LINE_COLOR = 0xffeeeeee;
    private static final int DEFAULT_GRID_HORIZONTAL_LINE_THICKNESS_DIP = 1;
    private static final int DEFAULT_GRID_HORIZONTAL_LINE_COLOR = 0xffeeeeee;
    private static final int DEFAULT_BOTTOM_TEXT_SIZE_SP = 12;
    private static final int DEFAULT_BOTTOM_TEXT_COLOR = 0xff9b9a9b;
    private static final boolean DEFAULT_DRAW_DOT_LINE = false;
    private static final int DEFAULT_POPUP_TEXT_SIZE_SP = 13;
    private static final int DEFAULT_POPUP_TEXT_COLOR = 0xffffffff;
    private static final int DEFAULT_LINES_THICKNESS_DIP = 2;
    private static final int DEFAULT_INNER_DOR_RADIUS_DIP = 2;
    private static final int DEFAULT_INNER_DOT_COLOR = 0xffffffff;
    private static final int DEFAULT_OUTER_DOT_RADIUS_DIP = 5;
    private static final boolean DEFAULT_SHOW_POPUP_ON_TOUCH = true;

    // Attributes
    private int backgroundColor;
    private int gridVerticalLineThickness;
    private int gridVerticalLineColor;
    private int gridHorizontalLineThickness;
    private int gridHorizontalLineColor;
    private int bottomTextSize;
    private int bottomTextColor;
    private int popupTextSize;
    private int popupTextColor;
    private int linesThickness;
    private int innerDotRadius;
    private int innerDotColor;
    private int outerDotRadius;

	public void setDrawDotLine(Boolean drawDotLine) {
		this.drawDotLine = drawDotLine;

        backgroundLinesHorizontalPaint.setPathEffect(drawDotLine
                ? new DashPathEffect(new float[]{ 10, 5, 10, 5 }, 1)
                : null);
	}

	private Runnable animator = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame = false;
            for(ArrayList<Dot> data : drawDotLists){
            	for(Dot dot : data){
                    dot.update();
                    if(!dot.isAtRest()){
                        needNewFrame = true;
                    }
                }
            }
            if (needNewFrame) {
                postDelayed(this, 25);
            }
            invalidate();
        }
    };

    public LineView(Context context){
        this(context, null);
    }

    public LineView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initAttributes(context, attrs);

        popupTextPaint.setAntiAlias(true);
        popupTextPaint.setColor(popupTextColor);
        popupTextPaint.setTextSize(popupTextSize);
        popupTextPaint.setStrokeWidth(5);
        popupTextPaint.setTextAlign(Paint.Align.CENTER);

        bottomTextPaint.setAntiAlias(true);
        bottomTextPaint.setTextSize(bottomTextSize);
        bottomTextPaint.setTextAlign(Paint.Align.CENTER);
        bottomTextPaint.setStyle(Paint.Style.FILL);
        bottomTextPaint.setColor(bottomTextColor);

        backgroundLinesVerticalPaint.setStyle(Paint.Style.STROKE);
        backgroundLinesVerticalPaint.setStrokeWidth(gridVerticalLineThickness);
        backgroundLinesVerticalPaint.setColor(gridVerticalLineColor);

        backgroundLinesHorizontalPaint.setStyle(Paint.Style.STROKE);
        backgroundLinesHorizontalPaint.setStrokeWidth(gridHorizontalLineThickness);
        backgroundLinesHorizontalPaint.setColor(gridHorizontalLineColor);

        dotOuterPaint.setAntiAlias(true);

        dotInnerPaint.setAntiAlias(true);
        dotInnerPaint.setColor(innerDotColor);

        linesPaint.setAntiAlias(true);
        linesPaint.setStrokeWidth(MyUtils.dip2px(getContext(), 2));

        setDrawDotLine(drawDotLine);

        popupPool = new PopupPool(context);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        backgroundColor = DEFAULT_BACKGROUND_COLOR;
        gridVerticalLineThickness = MyUtils.dip2px(context, DEFAULT_GRID_VERTICAL_LINE_THICKNESS_DIP);
        gridVerticalLineColor = DEFAULT_GRID_HORIZONTAL_LINE_COLOR;
        gridHorizontalLineThickness = MyUtils.dip2px(context, DEFAULT_GRID_HORIZONTAL_LINE_THICKNESS_DIP);
        gridHorizontalLineColor = DEFAULT_GRID_VERTICAL_LINE_COLOR;
        bottomTextSize = MyUtils.sp2px(context, DEFAULT_BOTTOM_TEXT_SIZE_SP);
        bottomTextColor = DEFAULT_BOTTOM_TEXT_COLOR;
        drawDotLine = DEFAULT_DRAW_DOT_LINE;
        popupTextSize = MyUtils.sp2px(context, DEFAULT_POPUP_TEXT_SIZE_SP);
        popupTextColor = DEFAULT_POPUP_TEXT_COLOR;
        popupsVisibilityMode = PopupsVisibilityMode.NONE;
        linesThickness = MyUtils.dip2px(context, DEFAULT_LINES_THICKNESS_DIP);
        innerDotRadius = MyUtils.dip2px(context, DEFAULT_INNER_DOR_RADIUS_DIP);
        innerDotColor = DEFAULT_INNER_DOT_COLOR;
        outerDotRadius = MyUtils.dip2px(context, DEFAULT_OUTER_DOT_RADIUS_DIP);
        showPopupOnTouch = DEFAULT_SHOW_POPUP_ON_TOUCH;

        if (attrs == null) {
            return;
        }

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LineView, 0, 0);
        try {
            backgroundColor = a.getColor(R.styleable.LineView_backgroundColor, backgroundColor);
            gridVerticalLineThickness = a.getDimensionPixelSize(R.styleable.LineView_gridVerticalLineThickness,
                    gridVerticalLineThickness);
            gridVerticalLineColor = a.getColor(R.styleable.LineView_gridVerticalLineColor, gridVerticalLineColor);
            gridHorizontalLineThickness = a.getDimensionPixelSize(R.styleable.LineView_gridHorizontalLineThickness,
                    gridHorizontalLineThickness);
            gridHorizontalLineColor = a.getColor(R.styleable.LineView_gridHorizontalLineColor, gridHorizontalLineColor);
            bottomTextSize = a.getDimensionPixelSize(R.styleable.LineView_bottomTextSize, bottomTextSize);
            bottomTextColor = a.getColor(R.styleable.LineView_bottomTextColor, bottomTextColor);
            drawDotLine = a.getBoolean(R.styleable.LineView_drawDotLine, drawDotLine);
            popupTextSize = a.getDimensionPixelSize(R.styleable.LineView_popupTextSize, popupTextSize);
            popupTextColor = a.getColor(R.styleable.LineView_popupTextColor, popupTextColor);
            popupsVisibilityMode = PopupsVisibilityMode.byIndex(
                    a.getInt(R.styleable.LineView_popupsVisibilityMode, popupsVisibilityMode.ordinal()));
            linesThickness = a.getDimensionPixelSize(R.styleable.LineView_linesThickness, linesThickness);
            innerDotRadius = a.getDimensionPixelSize(R.styleable.LineView_innerDotRadius, innerDotRadius);
            innerDotColor = a.getColor(R.styleable.LineView_innerDotColor, innerDotColor);
            outerDotRadius = a.getDimensionPixelSize(R.styleable.LineView_outerDotRadius, outerDotRadius);
            showPopupOnTouch = a.getBoolean(R.styleable.LineView_showPopupOnTouch, showPopupOnTouch);
        } finally {
            a.recycle();
        }
    }

    public PopupsVisibilityMode getPopupsVisibilityMode() {
        return popupsVisibilityMode;
    }

    public void setPopupsVisibilityMode(PopupsVisibilityMode popupsVisibilityMode) {
        this.popupsVisibilityMode = popupsVisibilityMode;
    }

    public boolean isShowPopupOnTouch() {
        return showPopupOnTouch;
    }

    public void setShowPopupOnTouch(boolean showPopupOnTouch) {
        this.showPopupOnTouch = showPopupOnTouch;
    }

    /**
     * dataList will be reset when called is method.
     * @param bottomTextList The String ArrayList in the bottom.
     */
    public void setBottomTextList(ArrayList<String> bottomTextList){
        this.bottomTextList = bottomTextList;

        Rect r = new Rect();
        int longestWidth = 0;
        String longestStr = "";
        bottomTextDescent = 0;
        for(String s:bottomTextList){
            bottomTextPaint.getTextBounds(s,0,s.length(),r);
            if(bottomTextHeight<r.height()){
                bottomTextHeight = r.height();
            }
            if(autoSetGridWidth&&(longestWidth<r.width())){
                longestWidth = r.width();
                longestStr = s;
            }
            if(bottomTextDescent<(Math.abs(r.bottom))){
                bottomTextDescent = Math.abs(r.bottom);
            }
        }

        if(autoSetGridWidth){
            if(backgroundGridWidth<longestWidth){
                backgroundGridWidth = longestWidth+(int)bottomTextPaint.measureText(longestStr,0,1);
            }
            if(sideLineLength<longestWidth/2){
                sideLineLength = longestWidth/2;
            }
        }

        refreshXCoordinateList(getHorizontalGridNum());
    }

    /**
     *
     * @param dataLists The Integer ArrayLists for showing,
     *                 dataList.size() must < bottomTextList.size()
     */
    public void setDataList(ArrayList<ArrayList<Integer>> dataLists){
    	selectedDot = null;
        this.dataLists = dataLists;
        for(ArrayList<Integer> list : dataLists){
        	if(list.size() > bottomTextList.size()){
                throw new RuntimeException("dacer.LineView error:" +
                        " dataList.size() > bottomTextList.size() !!!");
            }
        }
        int biggestData = 0;
        for(ArrayList<Integer> list : dataLists){
        	if(autoSetDataOfGird){
                for(Integer i:list){
                    if(biggestData<i){
                        biggestData = i;
                    }
                }
        	}
        	dataOfAGird = 1;
        	while(biggestData/10 > dataOfAGird){
        		dataOfAGird *= 10;
        	}
        }
        
        refreshAfterDataChanged();
        setMinimumWidth(0); // It can help the LineView reset the Width,
                                // I don't know the better way..
        postInvalidate();
    }

    private void refreshAfterDataChanged(){
        int verticalGridNum = getVerticalGridlNum();
        refreshTopLineLength(verticalGridNum);
        refreshYCoordinateList(verticalGridNum);
        refreshDrawDotList(verticalGridNum);
    }

    private int getVerticalGridlNum(){
        int verticalGridNum = MIN_VERTICAL_GRID_NUM;
        if(dataLists != null && !dataLists.isEmpty()){
        	for(ArrayList<Integer> list : dataLists){
	        	for(Integer integer:list){
	        		if(verticalGridNum<(integer+1)){
	        			verticalGridNum = integer+1;
	        		}
	        	}
        	}
        }
        return verticalGridNum;
    }

    private int getHorizontalGridNum(){
        int horizontalGridNum = bottomTextList.size()-1;
        if(horizontalGridNum<MIN_HORIZONTAL_GRID_NUM){
            horizontalGridNum = MIN_HORIZONTAL_GRID_NUM;
        }
        return horizontalGridNum;
    }

    private void refreshXCoordinateList(int horizontalGridNum){
        xCoordinateList.clear();
        for(int i=0;i<(horizontalGridNum+1);i++){
            xCoordinateList.add(sideLineLength + backgroundGridWidth*i);
        }

    }

    private void refreshYCoordinateList(int verticalGridNum){
        yCoordinateList.clear();
        for(int i=0;i<(verticalGridNum+1);i++){
            yCoordinateList.add(topLineLength +
                    ((mViewHeight-topLineLength-bottomTextHeight-bottomTextTopMargin-
                            bottomLineLength-bottomTextDescent)*i/(verticalGridNum)));
        }
    }

    private void refreshDrawDotList(int verticalGridNum){
        if(dataLists != null && !dataLists.isEmpty()){
    		if(drawDotLists.size() == 0){
    			for(int k = 0; k < dataLists.size(); k++){
    				drawDotLists.add(new ArrayList<LineView.Dot>());
    			}
    		}
        	for(int k = 0; k < dataLists.size(); k++){
        		int drawDotSize = drawDotLists.get(k).isEmpty()? 0:drawDotLists.get(k).size();
        		
        		for(int i=0;i<dataLists.get(k).size();i++){
                    int x = xCoordinateList.get(i);
                    int y = yCoordinateList.get(verticalGridNum - dataLists.get(k).get(i));
                    if(i>drawDotSize-1){
                    	//도트리스트를 추가한다.
                        drawDotLists.get(k).add(new Dot(x, 0, x, y, dataLists.get(k).get(i),k));
                    }else{
                    	//도트리스트에 타겟을 설정한다.
                        drawDotLists.get(k).set(i, drawDotLists.get(k).get(i).setTargetData(x,y,dataLists.get(k).get(i),k));
                    }
                }
        		
        		int temp = drawDotLists.get(k).size() - dataLists.get(k).size();
        		for(int i=0; i<temp; i++){
        			drawDotLists.get(k).remove(drawDotLists.get(k).size()-1);
        		}
        	}
        }
        removeCallbacks(animator);
        post(animator);
    }

    private void refreshTopLineLength(int verticalGridNum){
        // For prevent popup can't be completely showed when backgroundGridHeight is too small.
        // But this code not so good.
        if((mViewHeight-topLineLength-bottomTextHeight-bottomTextTopMargin)/
                (verticalGridNum+2)<getPopupHeight()){
            topLineLength = getPopupHeight() + outerDotRadius + innerDotRadius + 2;
        }else{
            topLineLength = MIN_TOP_LINE_LENGTH;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawBackgroundGrid(canvas);
        drawBottomText(canvas);
        drawLines(canvas);
        drawDots(canvas);
        drawPopups(canvas);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(backgroundColor);
    }

    private void drawBackgroundGrid(Canvas canvas) {
        drawGridVerticalLines(canvas);
        drawGridHorizontalLines(canvas);
    }

    private void drawGridVerticalLines(Canvas canvas) {
        final int verticalLineYEnd = mViewHeight - bottomTextTopMargin - bottomTextHeight - bottomTextDescent;

        for (int i = 0; i < xCoordinateList.size(); i++) {
            final int x = xCoordinateList.get(i);
            canvas.drawLine(x, 0, x, verticalLineYEnd, backgroundLinesVerticalPaint);
        }
    }

    private void drawGridHorizontalLines(Canvas canvas) {
        final int width = getWidth();

        if (drawDotLine) {
            tmpPath.reset();
        }

        for (int i = 0, count = yCoordinateList.size(); i < count; i++) {
            if ((count - 1 - i) % dataOfAGird != 0) {
                continue;
            }

            final int y = yCoordinateList.get(i);

            if (drawDotLine) {
                tmpPath.moveTo(0, y);
                tmpPath.lineTo(width, y);
                canvas.drawPath(tmpPath, backgroundLinesHorizontalPaint);
            } else {
                canvas.drawLine(0, y, width, y, backgroundLinesHorizontalPaint);
            }
        }
    }

    private void drawBottomText(Canvas canvas) {
        if (bottomTextList == null) {
            return;
        }

        for (int i = 0; i < bottomTextList.size(); i++) {
            canvas.drawText(bottomTextList.get(i), sideLineLength + backgroundGridWidth * i, mViewHeight - bottomTextDescent, bottomTextPaint);
        }
    }

    private void drawLines(Canvas canvas) {
        for(int i = 0; i < drawDotLists.size(); i++) {
            linesPaint.setColor(lineColors[i % 3]);

            for (int j = 0; j < drawDotLists.get(i).size() - 1; j++) {
                canvas.drawLine(drawDotLists.get(i).get(j).x,
                        drawDotLists.get(i).get(j).y,
                        drawDotLists.get(i).get(j+1).x,
                        drawDotLists.get(i).get(j+1).y,
                        linesPaint);
            }
        }
    }

    private void drawDots(Canvas canvas){
        if (drawDotLists.isEmpty()) {
            return;
        }

        for (int i = 0; i < drawDotLists.size(); i++) {
            dotOuterPaint.setColor(lineColors[i % 3]);

            for (Dot dot : drawDotLists.get(i)) {
                canvas.drawCircle(dot.x, dot.y, outerDotRadius, dotOuterPaint);
                canvas.drawCircle(dot.x, dot.y, innerDotRadius, dotInnerPaint);
            }
        }
    }

    private void drawPopups(Canvas canvas) {
        // FIXME check this. Look little bit confusing
        for (int k = 0; k < drawDotLists.size(); k++) {
            final int max = Collections.max(dataLists.get(k));
            final int min = Collections.min(dataLists.get(k));

            for (Dot d : drawDotLists.get(k)) {
                if (popupsVisibilityMode == PopupsVisibilityMode.ALL)
                    drawPopup(canvas, String.valueOf(d.data), d.setupPoint(tmpPoint), popupColors[k % 3]);
                else if (popupsVisibilityMode == PopupsVisibilityMode.MIN_MAX) {
                    if (d.data == max) {
                        drawPopup(canvas, String.valueOf(d.data), d.setupPoint(tmpPoint), popupColors[k % 3]);
                    }
                    if (d.data == min) {
                        drawPopup(canvas, String.valueOf(d.data), d.setupPoint(tmpPoint), popupColors[k % 3]);
                    }
                }
            }
        }

        if (showPopupOnTouch && selectedDot != null) {
            drawPopup(canvas,
                    String.valueOf(selectedDot.data),
                    selectedDot.setupPoint(tmpPoint),
                    popupColors[selectedDot.linenumber % 3]);
        }
    }

    private void drawPopup(Canvas canvas, String value, Point point, int popupColor) {
        final Context context = getContext();

        final int sidePadding = MyUtils.dip2px(context, value.length() == 1 ? 8 : 5);
        final int x = point.x;
        final int y = point.y - MyUtils.dip2px(context, 5);

        popupTextPaint.getTextBounds(value, 0, value.length(), tmpRect);

        final int textWidth = tmpRect.width();
        final int textHeight = tmpRect.height();
        tmpRect.set(x - textWidth / 2 - sidePadding,
                y - textHeight - bottomTriangleHeight - popupTopPadding * 2 - popupBottomMargin,
                x + textWidth / 2 + sidePadding,
                y + popupTopPadding - popupBottomMargin);

        Drawable popup = popupPool.getPopupDrawable(popupColor);
        popup.setBounds(tmpRect);
        popup.draw(canvas);

        canvas.drawText(value, x, y - bottomTriangleHeight - popupBottomMargin, popupTextPaint);
    }

    private int getPopupHeight(){
        Rect popupTextRect = new Rect();
        popupTextPaint.getTextBounds("9",0,1,popupTextRect);
        Rect r = new Rect(-popupTextRect.width()/2,
                 - popupTextRect.height()-bottomTriangleHeight-popupTopPadding*2-popupBottomMargin,
                 + popupTextRect.width()/2,
                +popupTopPadding-popupBottomMargin);
        return r.height();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mViewWidth = measureWidth(widthMeasureSpec);
        mViewHeight = measureHeight(heightMeasureSpec);
        refreshAfterDataChanged();
        setMeasuredDimension(mViewWidth,mViewHeight);
    }

    private int measureWidth(int measureSpec){
        int horizontalGridNum = getHorizontalGridNum();
        int preferred = backgroundGridWidth*horizontalGridNum+sideLineLength*2;
        return getMeasurement(measureSpec, preferred);
    }

    private int measureHeight(int measureSpec){
        int preferred = 0;
        return getMeasurement(measureSpec, preferred);
    }

    private int getMeasurement(int measureSpec, int preferred){
        int specSize = MeasureSpec.getSize(measureSpec);
        int measurement;
        switch(MeasureSpec.getMode(measureSpec)){
            case MeasureSpec.EXACTLY:
                measurement = specSize;
                break;
            case MeasureSpec.AT_MOST:
                measurement = Math.min(preferred, specSize);
                break;
            default:
                measurement = preferred;
                break;
        }
        return measurement;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (! showPopupOnTouch) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pointToSelect = findPointAt((int) event.getX(), (int) event.getY());
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (pointToSelect != null) {
                selectedDot = pointToSelect;
                pointToSelect = null;
                postInvalidate();
            }
        }

        return true;
    }

    private Dot findPointAt(int x, int y) {
        if (drawDotLists.isEmpty()) {
            return null;
        }

        final int width = backgroundGridWidth/2;
        final Region r = new Region();

        for (ArrayList<Dot> data : drawDotLists) {
            for (Dot dot : data) {
                final int pointX = dot.x;
                final int pointY = dot.y;

                r.set(pointX - width, pointY - width, pointX + width, pointY + width);
                if (r.contains(x, y)){
                    return dot;
                }
            }
        }

        return null;
    }


    
    class Dot{
        int x;
        int y;
        int data;
        int targetX;
        int targetY;
        int linenumber;
        int velocity = MyUtils.dip2px(getContext(),18);

        Dot(int x,int y,int targetX,int targetY,Integer data,int linenumber){
            this.x = x;
            this.y = y;
            this.linenumber = linenumber;
            setTargetData(targetX, targetY,data,linenumber);
        }

        Point setupPoint(Point point) {
            point.set(x, y);
            return point;
        }

        Dot setTargetData(int targetX,int targetY,Integer data,int linenumber){
            this.targetX = targetX;
            this.targetY = targetY;
            this.data = data;
            this.linenumber = linenumber;
            return this;
        }

        boolean isAtRest(){
            return (x==targetX)&&(y==targetY);
        }

        void update(){
            x = updateSelf(x, targetX, velocity);
            y = updateSelf(y, targetY, velocity);
        }

        private int updateSelf(int origin, int target, int velocity){
            if (origin < target) {
                origin += velocity;
            } else if (origin > target){
                origin-= velocity;
            }
            if(Math.abs(target-origin)<velocity){
                origin = target;
            }
            return origin;
        }
    }

    private static class PopupPool {

        private Resources resources;
        private Map<Integer, Drawable> pool = new HashMap<Integer, Drawable>();

        public PopupPool(Context context) {
            this.resources = context.getResources();
        }

        public Drawable getPopupDrawable(int color) {
            Drawable drawable;

            if (pool.containsKey(color)) {
                drawable = pool.get(color);
            } else {
                drawable = resources.getDrawable(R.drawable.ic_popup);
                drawable.mutate();
                drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                pool.put(color, drawable);
            }

            return drawable;
        }
    }
}
