package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.User;
import org.segrada.model.prototype.IUser;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.repository.UserRepository;
import org.segrada.service.repository.factory.RepositoryFactory;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * User service
 */
public class UserService extends AbstractRepositoryService<IUser, UserRepository> {
	/**
	 * Constructor
	 */
	@Inject
	public UserService(RepositoryFactory repositoryFactory) {
		super(repositoryFactory, UserRepository.class);
	}

	@Override
	public IUser createNewInstance() {
		return new User();
	}

	@Override
	public Class<IUser> getModelClass() {
		return IUser.class;
	}

	/**
	 * Find user by login name
	 * @param login login name
	 * @return user or null
	 */
	public  IUser findByLogin(String login) {
		return repository.findByLogin(login);
	}
}
