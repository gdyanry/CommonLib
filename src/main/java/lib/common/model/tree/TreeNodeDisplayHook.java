/**
 * 
 */
package lib.common.model.tree;

/**
 * @author yanry
 *
 * 2016年2月17日
 */
public interface TreeNodeDisplayHook<I> {

	String getJoint();
	
	String getNodeText(Node<I> node);
}
