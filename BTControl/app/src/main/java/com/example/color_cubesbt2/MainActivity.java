package com.example.color_cubesbt2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView mColorWheel;
    ImageView mSelectCircle;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mColorWheel = findViewById(R.id.ColorWheel);
        mSelectCircle = findViewById(R.id.SelectCircle);


        mColorWheel.setDrawingCacheEnabled(true);
        mColorWheel.buildDrawingCache(true);

        //handle movement of SelectCircle in ColorWheel on touch (ColorWheel image view)
        mColorWheel.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                bitmap = mColorWheel.getDrawingCache();
                // check if event is happening on bitmap
                if (event.getY() < bitmap.getHeight() && event.getY() >= 0 && event.getX() < bitmap.getWidth() && event.getX() >= 0) {

                    // load selected pixel
                    int pixel = bitmap.getPixel((int) event.getX(), (int) event.getY());

                    // offset variables to compensate for width and height of SelectCircle
                    double addX = mColorWheel.getX() - mSelectCircle.getWidth()/2;
                    double addY = mColorWheel.getY() - mSelectCircle.getHeight()/2;

                    // if in close proximity to the ColorWheel (in an image view, but outside actual color wheel)
                    // we move SelectCircle back to the closest point in actual color wheel
                    // to prevent laggy movement, when selecting color on the edges
                    if ((Color.red(pixel) == 0) && (Color.green(pixel) == 0) && (Color.blue(pixel) == 0)) {
                        // values of ColorWheel
                        double wheelOffset = mColorWheel.getWidth()/59*11;
                        double radius = mColorWheel.getWidth()/59*18.5;

                        // X and Y differences between circle center and touched point
                        double centerPixelXDiff = event.getX() - (wheelOffset + radius);
                        double centerPixelYDiff = event.getY() - (wheelOffset + radius);

                        // distance to the closest point on the wheel from touched point
                        // (we subtract radius from distance of center to touched point)
                        double pointToWheelEdgeDistance = Math.sqrt(centerPixelXDiff*centerPixelXDiff + centerPixelYDiff*centerPixelYDiff) - radius;

                        double XDiffYDiffRatio = centerPixelXDiff/centerPixelYDiff;

                        // calculating x and y differences between touched point and closest edge point
                        double xDiff = Math.sqrt(pointToWheelEdgeDistance*pointToWheelEdgeDistance*XDiffYDiffRatio*XDiffYDiffRatio/(XDiffYDiffRatio*XDiffYDiffRatio + 1));
                        double yDiff = Math.sqrt(pointToWheelEdgeDistance*pointToWheelEdgeDistance - xDiff*xDiff);

                        // adding calculated values to offset
                        if(centerPixelXDiff < 0) {
                            addX += xDiff;
                        }else {
                            addX -= xDiff;
                        }

                        if(centerPixelYDiff < 0) {
                            addY += yDiff;
                        }else {
                            addY -= yDiff;
                        }
                    }

                    // moving SelectCircle
                    mSelectCircle.setX(event.getX() + (int)(addX));
                    mSelectCircle.setY(event.getY() + (int)(addY));

                    return true;
                }
            }
            return false;
        });
    }

}