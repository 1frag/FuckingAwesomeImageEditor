package com.example.image_editor;

import android.content.ClipData;
import android.content.ClipDescription;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Segmentation extends Conductor{

    ImageView imageView;
    MainActivity activity;

    ImageView dragable;

    Segmentation(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        this.activity = activity;
        this.imageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.movable_view);

        dragable = activity.findViewById(R.id.test_test_test);
        dragable.setImageResource(R.drawable.ic_wall);
        dragable.setTag("hello");


        dragable.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData.Item item = new ClipData.Item((CharSequence)v.getTag());
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData dragData = new ClipData(v.getTag().toString(),
                        mimeTypes, item);

                // Instantiates the drag shadow builder.
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(dragable);

                // Starts the drag
                v.startDrag(dragData,  // the data to be dragged
                        myShadow,  // the drag shadow builder
                        null,      // no need to use local data
                        0          // flags (not currently used, set to 0)
                );
                return true;
            }
        });
        // Create and set the drag event listener for the View
        dragable.setOnDragListener( new View.OnDragListener(){
            @Override
            public boolean onDrag(View v,  DragEvent event){
                switch(event.getAction())
                {
                    case DragEvent.ACTION_DRAG_STARTED:
                        System.out.println("Action is DragEvent.ACTION_DRAG_STARTED");
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.d("msg", "Action is DragEvent.ACTION_DRAG_ENTERED");
                        break;
                    case DragEvent.ACTION_DRAG_EXITED :
                        Log.d("msg", "Action is DragEvent.ACTION_DRAG_EXITED");
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION  :
                        Log.d("msg", "Action is DragEvent.ACTION_DRAG_LOCATION");
                        int right=dragable.getRight();
                        int left=dragable.getLeft();
                        int top=dragable.getTop();
                        int bottom=dragable.getBottom();
                        System.out.println("Start Touch "+right+" "+top+" "+left+" "+bottom);
              /*  if(x_cord>left&&y_cord>top&&x_cord<right&&y_cord<bottom){
                   System.out.println("GONE");
                   ima.setVisibility(View.GONE);
               }*/
                        break;
                    case DragEvent.ACTION_DRAG_ENDED   :
                        System.out.println( "ACTION_DRAG_ENDED event");
                        break;
                    case DragEvent.ACTION_DROP:
                        Log.d("msg", "ACTION_DROP event");
                        break;
                    default: break;
                }
                return true;
            }
        });
    }
}
