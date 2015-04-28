package rs.pelotas.arch.resource;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import rs.pelotas.arch.enumeration.OrderBy;
import rs.pelotas.arch.enumeration.Param;

/**
 *
 * @author Rafael Guterres
 */
public class QueryParams {
        
    private static final String PARAM_DELIMITER_VALUES = ",";
    private static final String PARAM_ORDERBY_ASC = "+";
    private static final String PARAM_ORDERBY_DESC = "-";
    
    private Integer offset;
    private Integer limit;
    private List<String> fieldList;
    private List<Map<String, String>> sortList;
    private List<Map<String, String>> filterList;

    public QueryParams() {
    }

    public QueryParams(HttpServletRequest request) {
        setOffset(request.getParameter(Param.OFFSET.name().toLowerCase()));
        setLimit(request.getParameter(Param.LIMIT.name().toLowerCase()));
        setSortList(request.getParameter(Param.SORT.name().toLowerCase()));
        setFieldList(request.getParameter(Param.FIELDS.name().toLowerCase()));
        setFilterList(request);
    }

    public Integer getOffset() {
        return offset;
    }

    private void setOffset(String offset) {
        try {
            this.offset = Integer.parseInt(offset);
        } catch (NumberFormatException e) {
            this.offset = null;
        }
    }
    
    public Integer getLimit() {
        return limit;
    }

    private void setLimit(String limit) {
        try {
            this.limit = Integer.parseInt(limit);
        } catch (NumberFormatException e) {
            this.limit = null;
        }
    }
    
    public List<Map<String, String>> getSortList() {
        return sortList;
    }

    private void setSortList(String sortParams) {
        this.sortList = new ArrayList<>();
        if (sortParams != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(sortParams, PARAM_DELIMITER_VALUES);
            while(stringTokenizer.hasMoreTokens()) {
                Map<String, String> sort = new HashMap<>();
                String fieldParam = stringTokenizer.nextToken();
                String order;
                switch (fieldParam.substring(0, 1)) {
                    case PARAM_ORDERBY_DESC:
                        order = OrderBy.DESC.name();
                        fieldParam = fieldParam.substring(1);
                        break;
                    case PARAM_ORDERBY_ASC:
                        fieldParam = fieldParam.substring(1);
                    default:
                        order = OrderBy.ASC.name();
                        break;
                }
                sort.put(fieldParam, order);
                this.sortList.add(sort);
            }
        }
    }

    public List<String> getFieldList() {
        return fieldList;
    }

    private void setFieldList(String fieldParams) {
        this.fieldList = new ArrayList<>();
        if (fieldParams != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(fieldParams, PARAM_DELIMITER_VALUES);
            while(stringTokenizer.hasMoreTokens()) {
                this.fieldList.add(stringTokenizer.nextToken());
            }
        }
    }

    public List<Map<String, String>> getFilterList() {
        return filterList;
    }

    private void setFilterList(HttpServletRequest request) {
        this.filterList = new ArrayList<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String paramName = params.nextElement();
            try {
                Param.valueOf(paramName.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, String> filter = new HashMap<>();
                filter.put(paramName, request.getParameter(paramName));
                this.filterList.add(filter);
            }
        }
    }
}