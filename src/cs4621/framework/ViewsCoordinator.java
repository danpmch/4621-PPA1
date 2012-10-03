package cs4621.framework;

public interface ViewsCoordinator
{
	void setViewUpdated(int viewId);
	void resetUpdatedStatus();
	boolean checkAllViewsUpdated();
}
