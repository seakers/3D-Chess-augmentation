package tatc.util;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Gson Type Adapter that allows to read JSON files with fields that contain a single object even if they are declared
 * as lists in a java class. Without this adapter GSON wuld throw a throw an exception because it BEGIN_OBJECT where it
 * expects BEGIN_ARRAY.
 * Code source: https://stackoverflow.com/questions/43412261/make-gson-accept-single-objects-where-it-expects-arrays
 */
final public class AlwaysListTypeAdapterFactory<E> implements TypeAdapterFactory {

    // Gson can instantiate it itself
    private AlwaysListTypeAdapterFactory() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
        // If it's not a List -- just delegate the job to Gson and let it pick the best type adapter itself
        if ( !List.class.isAssignableFrom(typeToken.getRawType()) ) {
            return null;
        }
        // Resolving the list parameter type
        final Type elementType = resolveTypeArgument(typeToken.getType());
        @SuppressWarnings("unchecked")
        final TypeAdapter<E> elementTypeAdapter = (TypeAdapter<E>) gson.getAdapter(TypeToken.get(elementType));
        // Note that the always-list type adapter is made null-safe, so we don't have to check nulls ourselves
        @SuppressWarnings("unchecked")
        final TypeAdapter<List<E>> defaultListAdapter = gson.getAdapter((TypeToken<List<E>>) TypeToken.getParameterized(List.class, elementType));
        final TypeAdapter<T> alwaysListTypeAdapter = (TypeAdapter<T>) new AlwaysListTypeAdapter<>(elementTypeAdapter, defaultListAdapter).nullSafe();
        return alwaysListTypeAdapter;
    }

    private static Type resolveTypeArgument(final Type type) {
        // The given type is not parameterized?
        if ( !(type instanceof ParameterizedType) ) {
            // No, raw
            return Object.class;
        }
        final ParameterizedType parameterizedType = (ParameterizedType) type;
        return parameterizedType.getActualTypeArguments()[0];
    }

    private static final class AlwaysListTypeAdapter<E>
            extends TypeAdapter<List<E>> {

        private final TypeAdapter<E> elementTypeAdapter;
        private final TypeAdapter<List<E>> defaultListAdapter;

        private AlwaysListTypeAdapter(final TypeAdapter<E> elementTypeAdapter, TypeAdapter<List<E>> defaultListAdapter) {
            this.elementTypeAdapter = elementTypeAdapter;
            this.defaultListAdapter = defaultListAdapter;
        }

        @Override
        public void write(final JsonWriter out, final List<E> list)  throws IOException {
            defaultListAdapter.write(out, list);
        }

        @Override
        public List<E> read(final JsonReader in) throws IOException {
            // This is where we detect the list "type"
            final List<E> list = new ArrayList<>();
            final JsonToken token = in.peek();
            switch ( token ) {
                case BEGIN_ARRAY:
                    // If it's a regular list, just consume [, <all elements>, and ]
                    in.beginArray();
                    while ( in.hasNext() ) {
                        list.add(elementTypeAdapter.read(in));
                    }
                    in.endArray();
                    break;
                case BEGIN_OBJECT:
                case STRING:
                case NUMBER:
                case BOOLEAN:
                    // An object or a primitive? Just add the current value to the result list
                    list.add(elementTypeAdapter.read(in));
                    break;
                case NULL:
                    throw new AssertionError("Must never happen: check if the type adapter configured with .nullSafe()");
                case NAME:
                case END_ARRAY:
                case END_OBJECT:
                case END_DOCUMENT:
                    throw new MalformedJsonException("Unexpected token: " + token);
                default:
                    throw new AssertionError("Must never happen: " + token);
            }
            return list;
        }

    }

}
