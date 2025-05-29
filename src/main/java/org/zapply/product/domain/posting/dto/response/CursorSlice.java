package org.zapply.product.domain.posting.dto.response;

import java.util.List;

public record CursorSlice<T>(
        List<T> content,
        String nextCursor,
        boolean hasNext
) {}