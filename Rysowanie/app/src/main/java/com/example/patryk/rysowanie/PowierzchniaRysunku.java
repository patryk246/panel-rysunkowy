package com.example.patryk.rysowanie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

public class PowierzchniaRysunku extends SurfaceView implements SurfaceHolder.Callback,Runnable {
    private SurfaceHolder mPojemnik;
    private Thread mWatekRysujacy;
    private boolean mWatekPracuje = false;
    private Object mBlokada=new Object();
    private Canvas mKanwa=null;
    private Bitmap mBitmapa = null;
    private Paint mFarba = new Paint();
    float oldX=0, oldY=0, newX=0, newY=0;
    public PowierzchniaRysunku(Context context, AttributeSet attrs) {
        super(context, attrs);
// Pojemnik powierzchni - pozwala kontrolować i monitorować powierzchnię
        mPojemnik = getHolder();
        mPojemnik.addCallback(this);
//inicjalizacja innych elementów...
    }
    public void wznowRysowanie() {
// uruchomienie wątku rysującego
        mWatekRysujacy = new Thread(this);
        mWatekPracuje = true;
        mWatekRysujacy.start();
    }
    public void pauzujRysowanie() {
        mWatekPracuje = false;
    }
    //obsługa dotknięcia ekranu
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        //sekcja krytyczna – modyfikacja rysunku na wyłączność
        synchronized (mBlokada) {
            //modyfikacja rysunku...
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pauzujRysowanie();
                    wznowRysowanie();
                    mFarba.setColor(MainActivity.kolor);
                    mFarba.setStrokeWidth(2);
                    mFarba.setStyle(Paint.Style.FILL);
                    mKanwa.drawCircle(event.getX(),event.getY(), 5, mFarba);
                    oldX=event.getX();
                    oldY=event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    newX=event.getX();
                    newY=event.getY();
                    mKanwa.drawLine(oldX,oldY,newX,newY,mFarba);
                    oldX=newX;
                    oldY=newY;
                    break;
                case MotionEvent.ACTION_UP:
                    mKanwa.drawCircle(event.getX(),event.getY(), 5, mFarba);
                    break;
            }

        }
        return true;
    }
    //żeby lint nie wyświetlał ostrzeżeń - onTouchEvent i performClick trzeba
    //implementować razem
    public boolean performClick()
    {
        return super.performClick();
    }
    @Override
    public void run() {
        while (mWatekPracuje) {
            Canvas kanwa = null;
            try {
// sekcja krytyczna - żaden inny wątek nie może używać pojemnika
                synchronized (mPojemnik) {
                    // czy powierzchnia jest prawidłowa
                    if (!mPojemnik.getSurface().isValid()) continue;
                    // zwraca kanwę, na której można rysować, każdy piksel
                    // kanwy w prostokącie przekazanym jako parametr musi być
                    // narysowany od nowa inaczej: rozpoczęcie edycji
                    // zawartości kanwy
                    kanwa = mPojemnik.lockCanvas(null);
                    //sekcja krytyczna – dostęp do rysunku na wyłączność
                    synchronized (mBlokada) {
                        if (mWatekPracuje) {
                            //rysowanie na lokalnej kanwie...
                            if(MainActivity.isCleared){
                                mKanwa.drawARGB(255, 255, 255, 255);
                                MainActivity.isCleared=false;
                            }
                            kanwa.drawBitmap(mBitmapa, 0, 0, null);
                        }
                    }
                }
            } finally {
                // w bloku finally - gdyby wystąpił wyjątek w powyższym
                // powierzchnia zostanie zostawiona w spójnym stanie
                if (kanwa != null) {
                    // koniec edycji kanwy i wyświetlenie rysunku na ekranie
                    mPojemnik.unlockCanvasAndPost(kanwa);
                }
            }
            try {
                Thread.sleep(1000 / 60); // 60
            } catch (InterruptedException e) {
            }
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mBitmapa = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        mKanwa = new Canvas(mBitmapa);
// zmalowanie na biało
        mKanwa.drawARGB(255, 255, 255, 255);
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) { }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
// zatrzymanie rysowania
        mWatekPracuje = false;
    }
}