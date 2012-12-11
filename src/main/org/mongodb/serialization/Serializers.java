/**
 * Copyright (c) 2008 - 2012 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mongodb.serialization;

import org.bson.BSONReader;
import org.bson.BSONWriter;
import org.bson.BsonType;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.mongodb.MongoCommandDocument;
import org.mongodb.MongoDocument;
import org.mongodb.MongoException;
import org.mongodb.MongoFieldSelectorDocument;
import org.mongodb.MongoQueryFilterDocument;
import org.mongodb.MongoSortCriteriaDocument;
import org.mongodb.MongoUpdateOperationsDocument;
import org.mongodb.serialization.serializers.BooleanSerializer;
import org.mongodb.serialization.serializers.DateSerializer;
import org.mongodb.serialization.serializers.DoubleSerializer;
import org.mongodb.serialization.serializers.IntegerSerializer;
import org.mongodb.serialization.serializers.LongSerializer;
import org.mongodb.serialization.serializers.MongoDocumentSerializer;
import org.mongodb.serialization.serializers.NullSerializer;
import org.mongodb.serialization.serializers.ObjectIdSerializer;
import org.mongodb.serialization.serializers.StringSerializer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// TODO: this is crap, but it's a start

/**
 * Holder for all the serializer mappings.
 */
public class Serializers implements Serializer {
    private Map<Class, Serializer> classSerializerMap = new HashMap<Class, Serializer>();
    private Map<BsonType, Class> bsonTypeClassMap = new HashMap<BsonType, Class>();

    public Serializers(final Serializers serializers) {
        classSerializerMap = new HashMap<Class, Serializer>(serializers.classSerializerMap);
        bsonTypeClassMap = new HashMap<BsonType, Class>(serializers.bsonTypeClassMap);
    }

    public Serializers() {
    }

    public Serializer lookup(Class clazz) {
        return classSerializerMap.get(clazz);
    }

    /**
     * @param clazz      the class
     * @param bsonType   the BSON type that this serializer handles
     * @param serializer the serializer  @return the previously registered serializer for this class
     */
    public Serializer register(Class clazz, BsonType bsonType, Serializer serializer) {
        bsonTypeClassMap.put(bsonType, clazz);
        return classSerializerMap.put(clazz, serializer);

    }

    @Override
    public void serialize(final BSONWriter writer, final Class clazz, final Object value,
                          final BsonSerializationOptions options) {
        Serializer serializer = classSerializerMap.get(clazz);
        if (serializer == null) {
            throw new MongoException("No serializer for class " + clazz);
        }
        serializer.serialize(writer, clazz, value, options);
    }

    @Override
    public Object deserialize(final BSONReader reader, final Class clazz, final BsonSerializationOptions options) {
        Serializer serializer = classSerializerMap.get(clazz);
        // TODO: handle null case
        if (serializer == null) {
            throw new MongoException("No serializer for class " + clazz);
        }
        return serializer.deserialize(reader, clazz, options);
    }

    public Class findClassByBsonType(BsonType bsonType) {
        return bsonTypeClassMap.get(bsonType);
    }

    // TODO: find a proper way to do this...
    public static Serializers createDefaultSerializers() {
        Serializers serializers = new Serializers();
        serializers.register(MongoQueryFilterDocument.class, BsonType.DOCUMENT, new MongoDocumentSerializer(serializers));
        serializers.register(MongoSortCriteriaDocument.class, BsonType.DOCUMENT, new MongoDocumentSerializer(serializers));
        serializers.register(MongoUpdateOperationsDocument.class, BsonType.DOCUMENT, new MongoDocumentSerializer(serializers));
        serializers.register(MongoFieldSelectorDocument.class, BsonType.DOCUMENT, new MongoDocumentSerializer(serializers));
        serializers.register(MongoCommandDocument.class, BsonType.DOCUMENT, new MongoDocumentSerializer(serializers));
        serializers.register(MongoDocument.class, BsonType.DOCUMENT, new MongoDocumentSerializer(serializers));
        serializers.register(ObjectId.class, BsonType.OBJECT_ID, new ObjectIdSerializer());
        serializers.register(Integer.class, BsonType.INT32, new IntegerSerializer());
        serializers.register(Long.class, BsonType.INT64, new LongSerializer());
        serializers.register(String.class, BsonType.STRING, new StringSerializer());
        serializers.register(Double.class, BsonType.DOUBLE, new DoubleSerializer());
        serializers.register(Binary.class, BsonType.BINARY, new BinarySerializer());
        serializers.register(Date.class, BsonType.DATE_TIME, new DateSerializer());
        serializers.register(Boolean.class, BsonType.BOOLEAN, new BooleanSerializer());
        serializers.register(Void.class, BsonType.NULL, new NullSerializer());
        return serializers;
    }
}
