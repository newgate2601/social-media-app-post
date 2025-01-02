package com.example.social_media_app_post.base.filter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Filter<T> {
    private TypedQuery<T> typedQuery;

    public static FilterBuilder builder(Class<?> clazz, EntityManager entityManager) {
        return new FilterBuilder<>(clazz, entityManager);
    }

    public List<T> getList(){
        return typedQuery.getResultList();
    }

    public static class FilterBuilder<T> implements IFilterBuilder {
        private TypedQuery<T> typedQuery;
        private StringBuilder query;
        private Class<T> entityClazz;
        private EntityManager entityManager;
        private String logicOperator;
        private Map<Object, Object> paramMap;
        private Integer paramCount;
        private StringBuilder conditions;
        private String orderBy;
        private Boolean mustOpenParentheses;
        private Boolean mustCloseParentheses;

        public FilterBuilder(Class<T> clazz, EntityManager entityManager) {
            entityClazz = clazz;
            query = new StringBuilder("SELECT e FROM ").append(entityClazz.getSimpleName()).append(" e ");
            this.entityManager = entityManager;
            paramMap = new HashMap<>();
            paramCount = 1;
            conditions = new StringBuilder();
            orderBy = "";
            mustOpenParentheses = false;
            mustCloseParentheses = false;
        }

        @Override
        public FilterBuilder<T> search() {
            logicOperator = " OR ";
            mustOpenParentheses = true;
            if (Boolean.TRUE.equals(mustCloseParentheses)){
                conditions.append(" ) ");
            }
            mustCloseParentheses = false;
            return this;
        }

        @Override
        public FilterBuilder<T> filter() {
            logicOperator = " AND ";
            mustOpenParentheses = true;
            if (Boolean.TRUE.equals(mustCloseParentheses)){
                conditions.append(" ) ");
            }
            mustCloseParentheses = false;
            return this;
        }

        @Override
        public FilterBuilder<T> isContain(String fieldName, String value) {
            if (Objects.isNull(value)){
                return this;
            }
            paramMap.put(paramCount, "%" + escape(value) + "%");
            conditions.append(Boolean.TRUE.equals(mustOpenParentheses) ? this.logicOperator + " ( " : this.logicOperator)
                    .append("e.").append(fieldName).append(" LIKE ?").append(paramCount++);
            if (Boolean.TRUE.equals(mustOpenParentheses)){
                mustOpenParentheses = false;
            }
            mustCloseParentheses = true;
            return this;
        }

        @Override
        public FilterBuilder<T> isNotIn(String fieldName, Collection values) {
            if (Objects.isNull(values)){
                return this;
            }
            paramMap.put(paramCount, values);
            conditions.append(Boolean.TRUE.equals(mustOpenParentheses) ? this.logicOperator + " ( " : this.logicOperator)
                    .append("e.").append(fieldName).append(" NOT IN (?").append(paramCount++).append(")");
            if (Boolean.TRUE.equals(mustOpenParentheses)){
                mustOpenParentheses = false;
            }
            mustCloseParentheses = true;
            return this;
        }

        @Override
        public FilterBuilder<T> isIn(String fieldName, Collection values) {
            if (Objects.isNull(values)){
                return this;
            }
            paramMap.put(paramCount, values);
            conditions.append(Boolean.TRUE.equals(mustOpenParentheses) ? this.logicOperator + " ( " : this.logicOperator)
                    .append("e.").append(fieldName).append(" IN (?").append(paramCount++).append(")");
            if (Boolean.TRUE.equals(mustOpenParentheses)){
                mustOpenParentheses = false;
            }
            mustCloseParentheses = true;
            return this;
        }

        @Override
        public FilterBuilder<T> isEqual(String fieldName, Object value) {
            if (Objects.isNull(value)){
                return this;
            }
            paramMap.put(paramCount, value);
            conditions.append(Boolean.TRUE.equals(mustOpenParentheses) ? this.logicOperator + " ( " : this.logicOperator)
                    .append("e.").append(fieldName).append(" = ?").append(paramCount++);
            if (Boolean.TRUE.equals(mustOpenParentheses)){
                mustOpenParentheses = false;
            }
            mustCloseParentheses = true;
            return this;
        }

        @Override
        public FilterBuilder<T> orderBy(String fieldName, String orderType) {
            orderBy = " ORDER BY e." + fieldName + " " + orderType;
            return this;
        }

        @Override
        public FilterBuilder<T> isNull(String fieldName) {
            conditions.append(Boolean.TRUE.equals(mustOpenParentheses) ? this.logicOperator + " ( " : this.logicOperator)
                    .append("e.").append(fieldName).append(" IS NULL ");
            if (Boolean.TRUE.equals(mustOpenParentheses)){
                mustOpenParentheses = false;
            }
            mustCloseParentheses = true;
            return this;
        }

        @Override
        public FilterBuilder isNotNull(String fieldName) {
            conditions.append(Boolean.TRUE.equals(mustOpenParentheses) ? this.logicOperator + " ( " : this.logicOperator)
                    .append("e.").append(fieldName).append(" IS NOT NULL ");
            if (Boolean.TRUE.equals(mustOpenParentheses)){
                mustOpenParentheses = false;
            }
            mustCloseParentheses = true;
            return this;
        }

        @Override
        public Page<T> getPage(Pageable pageable) {
            this.typedQuery = genQuery();
            for (Object key : paramMap.keySet()) {
                Object value = paramMap.get(key);
                typedQuery.setParameter((int) key, value);
            }

            int pageNumber = pageable.getPageNumber();
            int pageSize = pageable.getPageSize();
            typedQuery.setFirstResult((pageNumber * pageSize));
            typedQuery.setMaxResults((pageNumber * pageSize) + pageSize);
            List<T> content = typedQuery.getResultList();
            if (Objects.isNull(content) || content.isEmpty()){
                return Page.empty();
            }
            long total = typedQuery.getResultStream().count();
            System.out.println(query);
            return new PageImpl<>(content, pageable, total);
        }

        public Filter<T> build(){
            for (Object key : paramMap.keySet()) {
                Object value = paramMap.get(key);
                typedQuery.setParameter((int) key, value);
            }
            System.out.println(query);
            return new Filter<>(this.typedQuery);
        }

        private TypedQuery<T> genQuery(){
            String newConditions = "";
            if (conditions.length() > 0){
                newConditions = new String(this.conditions);
                if (newConditions.startsWith(" AND")){
                    newConditions = newConditions.substring(4);
                }
                else if (newConditions.startsWith(" OR")){
                    newConditions = newConditions.substring(3);
                }
                query.append("WHERE ").append(newConditions)
                        .append(Boolean.TRUE.equals(mustCloseParentheses) ? ")" : "");
            }
            query.append(orderBy);
            System.out.println(query);
            return entityManager.createQuery(new String(query), entityClazz);
        }

        private String escape(@NonNull String value) {
            return value.replace("%", "\\%").replace("_", "\\_");
        }
    }
}
