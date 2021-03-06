/**
 * Copyright 2017-2019 Cloudera, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.hortonworks.registries.storage.impl.jdbc.provider.sql.query;

import com.hortonworks.registries.common.Schema;
import com.hortonworks.registries.storage.Storable;
import com.hortonworks.registries.storage.util.StorageUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractStorableUpdateQuery extends AbstractStorableSqlQuery {
    protected final List<Schema.Field> whereFields = new ArrayList<>();
    private final List<Pair<Schema.Field, Object>> bindings = new ArrayList<>();


    public AbstractStorableUpdateQuery(Storable storable) {
        super(storable);
        Map<String, Object> columnsToValues = storable.toMap();
        columns.forEach(col -> bindings.add(Pair.of(col, columnsToValues.get(col.getName()))));
        primaryKey.getFieldsToVal().forEach((f, o) -> {
            bindings.add(Pair.of(f, o));
            whereFields.add(f);
        });
        try {
            Optional<Pair<Field, Long>> versionFieldValue = StorageUtils.getVersionFieldValue(storable);
            if (versionFieldValue.isPresent()) {
                Pair<Field, Long> fv = versionFieldValue.get();
                Schema.Field versionField = Schema.Field.of(fv.getKey().getName(), Schema.fromJavaType(fv.getValue().getClass()));
                whereFields.add(versionField);
                // update only if its the previous
                bindings.add(Pair.of(versionField, fv.getValue() - 1));
            }
        } catch (Exception ex) {
            LOG.error("Got exception", ex);
        }
    }

    public List<Pair<Schema.Field, Object>> getBindings() {
        return bindings;
    }
}
