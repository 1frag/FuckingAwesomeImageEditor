/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.image_editor;

import android.graphics.Outline;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;


public class ElevationDragFragment extends Fragment {

    public static final String TAG = "ElevationDragFragment";

    /* The circular outline provider */
    private ViewOutlineProvider mOutlineProviderCircle;

    /* The current elevation of the floating view. */
    private float mElevation = 0;

    /* The step in elevation when changing the Z value */
    private int mElevationStep;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOutlineProviderCircle = new CircleOutlineProvider();

        mElevationStep = getResources().getDimensionPixelSize(R.dimen.elevation_step);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.ztranslation, container, false);

        /* Find the {@link View} to apply z-translation to. */
        final View floatingShape1 = rootView.findViewById(R.id.circle1);
        final View floatingShape2 = rootView.findViewById(R.id.circle2);
        final View floatingShape3 = rootView.findViewById(R.id.circle3);
        final View floatingShape4 = rootView.findViewById(R.id.circle4);
        final View floatingShape5 = rootView.findViewById(R.id.circle5);
        final View floatingShape6 = rootView.findViewById(R.id.circle6);

        /* Define the shape of the {@link View}'s shadow by setting one of the {@link Outline}s. */
        floatingShape1.setOutlineProvider(mOutlineProviderCircle);
        floatingShape2.setOutlineProvider(mOutlineProviderCircle);
        floatingShape3.setOutlineProvider(mOutlineProviderCircle);
        floatingShape4.setOutlineProvider(mOutlineProviderCircle);
        floatingShape5.setOutlineProvider(mOutlineProviderCircle);
        floatingShape6.setOutlineProvider(mOutlineProviderCircle);

        /* Clip the {@link View} with its outline. */
        floatingShape1.setClipToOutline(true);
        floatingShape2.setClipToOutline(true);
        floatingShape3.setClipToOutline(true);
        floatingShape4.setClipToOutline(true);
        floatingShape5.setClipToOutline(true);
        floatingShape6.setClipToOutline(true);

        DragFrameLayout dragLayout = ((DragFrameLayout) rootView.findViewById(R.id.circle_main_layout));

        dragLayout.setDragFrameController(new DragFrameLayout.DragFrameLayoutController() {

            @Override
            public void onDragDrop(boolean captured) {
                /* Animate the translation of the {@link View}. Note that the translation
                 is being modified, not the elevation. */
                floatingShape1.animate()
                        .translationZ(captured ? 50 : 0)
                        .setDuration(100);
                floatingShape2.animate()
                        .translationZ(captured ? 50 : 0)
                        .setDuration(100);
                floatingShape3.animate()
                        .translationZ(captured ? 50 : 0)
                        .setDuration(100);
                floatingShape4.animate()
                        .translationZ(captured ? 50 : 0)
                        .setDuration(100);
                floatingShape5.animate()
                        .translationZ(captured ? 50 : 0)
                        .setDuration(100);
                floatingShape6.animate()
                        .translationZ(captured ? 50 : 0)
                        .setDuration(100);
                Log.i(TAG, captured ? "Drag" : "Drop");
            }
        });

        dragLayout.addDragView(floatingShape1);
        dragLayout.addDragView(floatingShape2);
        dragLayout.addDragView(floatingShape3);
        dragLayout.addDragView(floatingShape4);
        dragLayout.addDragView(floatingShape5);
        dragLayout.addDragView(floatingShape6);

        return rootView;
    }

    /**
     * ViewOutlineProvider which sets the outline to be an oval which fits the view bounds.
     */
    private class CircleOutlineProvider extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setOval(0, 0, view.getWidth(), view.getHeight());
        }
    }

}