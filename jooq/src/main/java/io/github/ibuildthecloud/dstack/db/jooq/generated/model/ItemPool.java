/**
 * This class is generated by jOOQ
 */
package io.github.ibuildthecloud.dstack.db.jooq.generated.model;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.2.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
@javax.persistence.Entity
@javax.persistence.Table(name = "item_pool", schema = "dstack")
public interface ItemPool extends java.io.Serializable {

	/**
	 * Getter for <code>dstack.item_pool.id</code>. 
	 */
	@javax.persistence.Id
	@javax.persistence.Column(name = "id", unique = true, nullable = false, precision = 19)
	public java.lang.Long getId();

	/**
	 * Getter for <code>dstack.item_pool.name</code>. 
	 */
	@javax.persistence.Column(name = "name", nullable = false, length = 255)
	public java.lang.String getName();

	/**
	 * Getter for <code>dstack.item_pool.kind</code>. 
	 */
	@javax.persistence.Column(name = "kind", nullable = false, length = 255)
	public java.lang.String getKind();

	/**
	 * Getter for <code>dstack.item_pool.state</code>. 
	 */
	@javax.persistence.Column(name = "state", nullable = false, length = 255)
	public java.lang.String getState();

	/**
	 * Getter for <code>dstack.item_pool.qualifier</code>. 
	 */
	@javax.persistence.Column(name = "qualifier", nullable = false, length = 255)
	public java.lang.String getQualifier();

	/**
	 * Getter for <code>dstack.item_pool.item_pool_generator_id</code>. 
	 */
	@javax.persistence.Column(name = "item_pool_generator_id", precision = 19)
	public java.lang.Long getItemPoolGeneratorId();

	/**
	 * Getter for <code>dstack.item_pool.segment</code>. 
	 */
	@javax.persistence.Column(name = "segment", length = 255)
	public java.lang.String getSegment();

	/**
	 * Getter for <code>dstack.item_pool.description</code>. 
	 */
	@javax.persistence.Column(name = "description", length = 255)
	public java.lang.String getDescription();

	/**
	 * Getter for <code>dstack.item_pool.uuid</code>. 
	 */
	@javax.persistence.Column(name = "uuid", nullable = false, length = 128)
	public java.lang.String getUuid();

	/**
	 * Getter for <code>dstack.item_pool.data</code>. 
	 */
	@javax.persistence.Column(name = "data", length = 16777215)
	public java.util.Map<String,Object> getData();
}