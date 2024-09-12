/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React, { FC } from "react";
import { Repository } from "@scm-manager/ui-types";
import MasterDetailsView from "./MasterDetailsView";

type Props = {
  link: string;
  repository: Repository;
};

const RepositoryWebhookConfiguration: FC<Props> = props => (
  <MasterDetailsView
    baseRoute={`/repo/${props.repository.namespace}/${props.repository.name}/settings/webhook`}
    {...props}
  />
);

export default RepositoryWebhookConfiguration;
