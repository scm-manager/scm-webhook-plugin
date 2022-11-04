package sonia.scm.webhook;

import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

@ExtensionPoint
public interface WebHookExecutorProvider<T extends SingleWebHookConfiguration> {

  boolean handles(Class<WebHook> webHookClass);

  WebHookExecutor createExecutor(T webHook, Repository repository, Iterable<Changeset> changesets);
}
