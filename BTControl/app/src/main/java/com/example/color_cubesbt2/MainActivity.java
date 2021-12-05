package com.example.color_cubesbt2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
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
                        int wheelOffset = Math.round(mColorWheel.getWidth()/59*11);
                        int radius = Math.round((float) (mColorWheel.getWidth()/59*18.5));

                        Point outerPoint = new Point((int) event.getX(),(int) event.getY());
                        Point outerPointOffset = getOffsetFromWheelEdge(wheelOffset, radius, outerPoint);

                        addX += outerPointOffset.x;
                        addY += outerPointOffset.y;
                    }

                    // moving SelectCircle
                    mSelectCircle.setX(event.getX() + Math.round(addX));
                    mSelectCircle.setY(event.getY() + Math.round(addY));

                    int selectedColor = bitmap.getPixel((int) mSelectCircle.getX() + mSelectCircle.getWidth()/2, (int) mSelectCircle.getY() + mSelectCircle.getHeight()/2);

                    int finalColor = changeColorIntensity(selectedColor, 255);

                    return true;
                }
            }
            return false;
        });
    }

    public int changeColorIntensity(int color, int newIntensity) {
        color = Color.rgb(255,255,0);
        int currentIntensity = Color.red(color) + Color.green(color) + Color.blue(color);
        double oneColorPointInNewIntensity = (double)newIntensity/(double)currentIntensity;
        int newColor = Color.rgb((int) (Color.red(color)*oneColorPointInNewIntensity), (int) (Color.green(color)*oneColorPointInNewIntensity), (int) (Color.blue(color)*oneColorPointInNewIntensity));
        return newColor;
    }


    // returns x and y difference of point from edge of a circle, which center is offset + radius far from coordination field.
    // Point object is returned
    public Point getOffsetFromWheelEdge(double wheelOffset, double wheelRadius, Point outerPoint) {

        // X and Y differences between circle center and touched point
        double centerPixelXDiff = outerPoint.x - (wheelOffset + wheelRadius);
        double centerPixelYDiff = outerPoint.y - (wheelOffset + wheelRadius);

        // distance to the closest point on the wheel from touched point
        // (we subtract radius from distance of center to touched point)
        double pointToWheelEdgeDistance = Math.sqrt(centerPixelXDiff*centerPixelXDiff + centerPixelYDiff*centerPixelYDiff) - wheelRadius;

        double XDiffYDiffRatio = centerPixelXDiff/centerPixelYDiff;

        // calculating x and y differences between touched point and closest edge point
        double xDiff = Math.sqrt(pointToWheelEdgeDistance*pointToWheelEdgeDistance*XDiffYDiffRatio*XDiffYDiffRatio/(XDiffYDiffRatio*XDiffYDiffRatio + 1));
        double yDiff = Math.sqrt(pointToWheelEdgeDistance*pointToWheelEdgeDistance - xDiff*xDiff);

        Point calculatedDiff = new Point(0,0);
        // adding calculated values to offset
        if(centerPixelXDiff < 0) {
            calculatedDiff.x = (int) xDiff;
        }else {
            calculatedDiff.x = -(int) xDiff;
        }

        if(centerPixelYDiff < 0) {
            calculatedDiff.y = (int) yDiff;
        }else {
            calculatedDiff.y = -(int) yDiff;
        }
        return calculatedDiff;
    }

}