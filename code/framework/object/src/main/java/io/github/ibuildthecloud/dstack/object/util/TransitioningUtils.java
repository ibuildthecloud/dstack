package io.github.ibuildthecloud.dstack.object.util;

import io.github.ibuildthecloud.dstack.archaius.util.ArchaiusUtil;
import io.github.ibuildthecloud.dstack.eventing.exception.EventExecutionException;
import io.github.ibuildthecloud.dstack.eventing.model.Event;
import io.github.ibuildthecloud.dstack.object.meta.ObjectMetaDataManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.netflix.config.DynamicBooleanProperty;

public class TransitioningUtils {

    public static final DynamicBooleanProperty SHOW_INTERNAL_MESSAGES =
            ArchaiusUtil.getBoolean("api.show.transitioning.internal.message");

    public static Map<String,Object> getTransitioningData(EventExecutionException e) {
        if ( e == null ) {
            return Collections.emptyMap();
        }

        return getTransitioningData(e.getEvent());
    }

    public static Map<String,Object> getTransitioningData(Event event) {
        if ( event == null || ! ObjectMetaDataManager.TRANSITIONING_ERROR.equals(event.getTransitioning()) ) {
            return Collections.emptyMap();
        }

        return getTransitioningData(event.getTransitioningMessage(), event.getTransitioningInternalMessage());
    }

    public static Map<String,Object> getTransitioningData(String message, String internalMessage) {
        Map<String,Object> data = new HashMap<String, Object>();

        String finalMessage = message == null ? "" : message;

        if ( SHOW_INTERNAL_MESSAGES.get() && ! StringUtils.isBlank(internalMessage) ) {
                finalMessage = finalMessage + " : " + internalMessage;
        }

        if ( StringUtils.isBlank(finalMessage) ) {
            finalMessage = null;
        }

        data.put(ObjectMetaDataManager.TRANSITIONING_FIELD,
                finalMessage == null ? null : ObjectMetaDataManager.TRANSITIONING_ERROR);
        data.put(ObjectMetaDataManager.TRANSITIONING_MESSAGE_FIELD,
                finalMessage);

        return data;
    }

}