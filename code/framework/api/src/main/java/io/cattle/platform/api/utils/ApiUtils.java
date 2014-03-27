package io.cattle.platform.api.utils;

import io.cattle.platform.api.auth.Policy;
import io.cattle.platform.api.auth.impl.DefaultPolicy;
import io.cattle.platform.object.util.DataUtils;
import io.cattle.platform.object.util.ObjectUtils;
import io.github.ibuildthecloud.gdapi.context.ApiContext;
import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.id.IdFormatter;
import io.github.ibuildthecloud.gdapi.model.Collection;
import io.github.ibuildthecloud.gdapi.model.Resource;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.gdapi.model.impl.WrappedResource;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;

public class ApiUtils {

    public static final String SINGLE_ATTACHMENT_PREFIX = "s__";

    private static final Policy DEFAULT_POLICY = new DefaultPolicy();

    public static Object getFirstFromList(Object obj) {
        if ( obj instanceof Collection ) {
            return getFirstFromList(((Collection)obj).getData());
        }

        if ( obj instanceof List ) {
            List<?> list = (List<?>)obj;
            return list.size() > 0 ? list.get(0) : null;
        }

        return null;
    }

    public static Policy getPolicy() {
        Object policy = ApiContext.getContext().getPolicy();
        if ( policy instanceof Policy ) {
            return (Policy)policy;
        } else {
            return DEFAULT_POLICY;
        }
    }

    public static <T> List<T> authorize(List<T> list) {
        return getPolicy().authorizeList(list);
    }

    @SuppressWarnings("unchecked")
    public static <T> T authorize(T obj) {
        if ( obj instanceof List ) {
            return (T) authorize((List<T>)obj);
        }
        return getPolicy().authorizeObject(obj);
    }

    public static String getAttachementKey(Object obj) {
        return getAttachementKey(obj, ObjectUtils.getId(obj));
    }

    public static String getAttachementKey(Object obj, Object id) {
        if ( obj == null ) {
            return null;
        }

        if ( id == null ) {
            return null;
        }

        return obj.getClass().getName() + ":" + id;
    }

    public static void addAttachement(Object key, String name, Object obj) {
        ApiRequest request = ApiContext.getContext().getApiRequest();
        @SuppressWarnings("unchecked")
        Map<String,Map<Object,Object>> attachments = (Map<String, Map<Object,Object>>) request.getAttribute(key);

        Object id = ObjectUtils.getId(obj);
        if ( id == null ) {
            return;
        }

        if ( attachments == null ) {
            attachments = new HashMap<String, Map<Object,Object>>();
            request.setAttribute(key, attachments);
        }

        Map<Object,Object> attachment = attachments.get(name);
        if ( attachment == null ) {
            attachment = new LinkedHashMap<Object, Object>();
            attachments.put(name, attachment);
        }

        attachment.put(id, obj);
    }

    public static Map<String,Object> getAttachements(Object obj, Transformer transformer) {
        Map<String,Object> result = new LinkedHashMap<String, Object>();
        Object key = getAttachementKey(obj);
        ApiRequest request = ApiContext.getContext().getApiRequest();
        @SuppressWarnings("unchecked")
        Map<String,Map<Object,Object>> attachments = (Map<String, Map<Object,Object>>) request.getAttribute(key);

        if ( attachments == null ) {
            return result;
        }

        for ( Map.Entry<String, Map<Object,Object>> entry : attachments.entrySet() ) {
            String keyName = entry.getKey();
            List<Object> objects = new ArrayList<Object>();
            for ( Object attachment : entry.getValue().values() ) {
                attachment = transformer.transform(attachment);
                if ( attachment != null ) {
                    objects.add(attachment);
                }
            }

            if ( keyName.startsWith(SINGLE_ATTACHMENT_PREFIX) ) {
                Object attachedObj = objects.size() > 0 ? objects.get(0) : null;
                result.put(keyName.substring(SINGLE_ATTACHMENT_PREFIX.length()), attachedObj);
            } else {
                result.put(keyName, objects);
            }
        }

        return result;
    }

    public static Resource createResourceWithAttachments(final SchemaFactory schemaFactory, final IdFormatter idFormatter,
            final Schema schema, Object obj, Map<String,Object> inputAdditionalFields) {
        Map<String,Object> additionalFields = new LinkedHashMap<String, Object>();
        additionalFields.putAll(DataUtils.getFields(obj));

        if ( inputAdditionalFields != null && inputAdditionalFields.size() > 0 ) {
            additionalFields.putAll(inputAdditionalFields);
        }

        Map<String,Object> attachments = ApiUtils.getAttachements(obj, new Transformer() {
            @Override
            public Object transform(Object input) {
                input = ApiUtils.authorize(input);
                if ( input == null )
                    return null;

                Schema schema = getSchemaForDisplay(schemaFactory, input);
                if ( schema == null ) {
                    return null;
                }

                return new WrappedResource(idFormatter, schema, input, DataUtils.getFields(input));
            }
        });

        additionalFields.putAll(attachments);

        return new WrappedResource(idFormatter, schema, obj, additionalFields);
    }

    public static Schema getSchemaForDisplay(SchemaFactory factory, Object obj) {
        Schema schema = factory.getSchema(obj.getClass());
        if ( schema.getChildren().size() > 0 ) {
            String kind = ObjectUtils.getKind(obj);
            Schema kindSchema = factory.getSchema(kind);
            if ( kindSchema != null ) {
                return kindSchema;
            }
        }

        return schema;
    }

}
