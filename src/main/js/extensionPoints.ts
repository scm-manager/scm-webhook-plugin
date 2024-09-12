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

import { ExtensionPointDefinition } from "@scm-manager/ui-extensions";
import { ComponentType } from "react";

export type WebhookConfiguration<ConfigType = never> = ExtensionPointDefinition<
  "webhook.configuration",
  {
    name: string;
    defaultConfiguration: ConfigType;
    FormComponent: ComponentType<{ webhook: ConfigType }>;
    OverviewCardTop?: ComponentType<{ webhook: ConfigType }>;
    OverviewCardBottom?: ComponentType<{ webhook: ConfigType }>;
  },
  any
>;
