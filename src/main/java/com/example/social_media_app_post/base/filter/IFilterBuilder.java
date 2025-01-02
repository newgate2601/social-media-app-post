package com.example.social_media_app_post.base.filter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

public interface IFilterBuilder<T> {
    Filter.FilterBuilder<T> search();
    Filter.FilterBuilder<T> filter();
    Filter.FilterBuilder<T> isContain(String fieldName, String value);
    Filter.FilterBuilder<T> isIn(String fieldName, Collection<Object> value);
    Filter.FilterBuilder<T> isNotIn(String fieldName, Collection<Object> values);
    Filter.FilterBuilder<T> isEqual(String fieldName, Object value);
    Filter.FilterBuilder<T> orderBy(String fieldName, String orderType);
    Filter.FilterBuilder<T> isNull(String fieldName);
    Filter.FilterBuilder<T> isNotNull(String fieldName);
    Page<T> getPage(Pageable pageable);
}
