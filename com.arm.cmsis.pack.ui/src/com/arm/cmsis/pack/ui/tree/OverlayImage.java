package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * This class is responsible for delivering an overlay image used e.g. for
 * decorators.
 * 
 */
public class OverlayImage extends CompositeImageDescriptor {

    public enum OverlayPos {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
    }

    private OverlayPos overlayPos;
    private Image baseImage;
    private Image overlayImage;
    private Point baseImageSize;

    public OverlayImage(Image baseImage, Image overlayImage, OverlayPos overlayPos) {
        this.baseImage = baseImage;
        this.overlayImage = overlayImage;
        this.overlayPos = overlayPos;

        baseImageSize = new Point(baseImage.getBounds().width, baseImage.getBounds().height);
    }

    @Override
    protected void drawCompositeImage(int arg0, int arg1) {

        // Draw the base image
        drawImage(createCachedImageDataProvider(baseImage), 0, 0);

        // draw overlay image
        Point pos = new Point(0, 0);
        Point overlayImageSize = new Point(overlayImage.getBounds().width, overlayImage.getBounds().height);

        switch (overlayPos) {
        case TOP_LEFT:
            break;
        case TOP_RIGHT:
            pos.x = baseImageSize.x - overlayImageSize.x;
            break;
        case BOTTOM_LEFT:
            pos.y = baseImageSize.y - overlayImageSize.y;
            break;
        case BOTTOM_RIGHT:
            pos.x = baseImageSize.x - overlayImageSize.x;
            pos.y = baseImageSize.y - overlayImageSize.y;
            break;
        case CENTER:
            pos.x = baseImageSize.x / 2;
            pos.y = baseImageSize.y / 2;
            break;
        }

        drawImage(createCachedImageDataProvider(overlayImage), pos.x, pos.y); // draw overlay image at position

    }

    @Override
    protected Point getSize() {
        return baseImageSize;
    }

    public Image getImage() {
        return createImage();
    }

}