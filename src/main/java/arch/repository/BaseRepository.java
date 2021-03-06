package arch.repository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import arch.entity.BaseEntity;
import arch.helper.Field;
import arch.helper.Reflection;

/**
 *
 * @author Rafael Guterres
 * @param <T>
 * @param <I>
 */
public abstract class BaseRepository<T extends BaseEntity, I extends Serializable> implements Repository<T, I> {

    private static final long serialVersionUID = 1946014114445975865L;

    private final Class<T> entityClass = Reflection.getGenericArgumentType(getClass());

    @Inject
    private EntityManager entityManager;

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public T load(I id) {
        T entity = getEntityManager().find(entityClass, id);
        return entity;
    }

    @Transactional
    @Override
    public void persist(T entity) {
        getEntityManager().persist(entity);
    }

    @Transactional
    @Override
    public T merge(T entity) {
        return (T) getEntityManager().merge(entity);
    }

    @Transactional
    @Override
    public void remove(I id) {
        T entity = load(id);
        if (entity != null) {
            getEntityManager().remove(entity);
        }
    }

    @Override
    public List<T> find() {
        return find(null, null, null);
    }

    @Override
    public Long count() {
        return count(null, null, null);
    }

    @Override
    public List<T> find(Integer offset, Integer limit) {
        return find(null, offset, limit);
    }

    @Override
    public Long count(Integer offset, Integer limit) {
        return count(null, offset, limit);
    }

    @Override
    public List<T> find(Filter filter) {
        return find(filter, null, null);
    }

    @Override
    public Long count(Filter filter) {
        return count(filter, null, null);
    }

    @Override
    public List<T> find(Filter filter, Integer offset, Integer limit) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<T> root = criteriaQuery.from(entityClass);
        criteriaQuery.select(root);
        Map<String, Object> parameters = new HashMap<>();
        if (filter != null) {
            Criteria.addWhere(criteriaBuilder, criteriaQuery, root, filter, parameters);
        }
        TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);
        if (!parameters.isEmpty()) {
            addQueryParameters(query, parameters);
        }
        if (offset != null && limit != null) {
            return getResultList(query, offset, limit);
        } else {
            return getResultList(query);
        }
    }

    @Override
    public Long count(Filter filter, Integer offset, Integer limit) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = criteriaQuery.from(entityClass);
        criteriaQuery.select(criteriaBuilder.count(root));
        Map<String, Object> parameters = new HashMap<>();
        if (filter != null) {
            Criteria.addWhere(criteriaBuilder, criteriaQuery, root, filter, parameters);
        }
        TypedQuery<Long> query = getEntityManager().createQuery(criteriaQuery);
        if (offset != null && limit != null) {
            return getCount(query, offset, limit);
        } else {
            return getCount(query);
        }
    }

    @Override
    public List<T> find(List<Field> filterList, List<Field> sortList, Integer offset, Integer limit) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<T> root = criteriaQuery.from(entityClass);
        criteriaQuery.select(root);
        Map<String, Object> parameters = new HashMap<>();
        if (filterList != null && !filterList.isEmpty()) {
            Criteria.addWhere(criteriaBuilder, criteriaQuery, root, filterList, parameters);
        }
        if (sortList != null && !sortList.isEmpty()) {
            Criteria.addOrderBy(criteriaBuilder, criteriaQuery, root, sortList);
        }
        TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);
        return getResultList(query, offset, limit);
    }

    @Override
    public Long count(List<Field> fieldList, List<Field> sortList, Integer offset, Integer limit) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = criteriaQuery.from(entityClass);
        criteriaQuery.select(criteriaBuilder.count(root));
        Map<String, Object> parameters = new HashMap<>();
        if (fieldList != null && !fieldList.isEmpty()) {
            Criteria.addWhere(criteriaBuilder, criteriaQuery, root, fieldList, parameters);
        }
        TypedQuery<Long> query = getEntityManager().createQuery(criteriaQuery);
        return getCount(query, offset, limit);
    }

    private void addQueryParameters(TypedQuery query, Map<String, Object> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                //query.setParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    private List<T> getResultList(Query query) {
        List<T> entities = query.getResultList();
        return entities;
    }

    private List<T> getResultList(Query query, Integer offset, Integer limit) {
        List<T> entities = query.setFirstResult(offset)
                                .setMaxResults(limit)
                                .getResultList();
        return entities;
    }

    private Long getCount(TypedQuery<Long> query) {
        Long count = query.getSingleResult();
        return count;
    }

    private Long getCount(TypedQuery<Long> query, Integer offset, Integer limit) {
        Long count = query.setFirstResult(offset)
                          .setMaxResults(limit)
                          .getSingleResult();
        return count;
    }

    @Override
    public Path getPath(Root root, String strPath) {
        Path path = root;
        String[] fields = strPath.split("\\.");
        for (String field : fields) {
            path = path.get(field);
        }
        return path;
    }
}
