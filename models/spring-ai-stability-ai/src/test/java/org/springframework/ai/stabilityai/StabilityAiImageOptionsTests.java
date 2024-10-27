/*
 * Copyright 2024 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ai.stabilityai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class StabilityAiImageOptionsTests {

	@Test
	void shouldPreferRuntimeOptionsOverDefaultOptions() {

		StabilityAiApi stabilityAiApi = mock(StabilityAiApi.class);
		// Default options
		StabilityAiImageOptions defaultOptions = StabilityAiImageOptions.builder()
			.withN(1)
			.withModel("default-model")
			.withWidth(512)
			.withHeight(512)
			.withResponseFormat("image/png")
			.withCfgScale(7.0f)
			.withClipGuidancePreset("FAST_BLUE")
			.withSampler("DDIM")
			.withSeed(1234L)
			.withSteps(30)
			.withStylePreset("3d-model")
			.build();

		// Runtime options with different values
		StabilityAiImageOptions runtimeOptions = StabilityAiImageOptions.builder()
			.withN(2)
			.withModel("runtime-model")
			.withWidth(1024)
			.withHeight(768)
			.withResponseFormat("application/json")
			.withCfgScale(14.0f)
			.withClipGuidancePreset("FAST_GREEN")
			.withSampler("DDPM")
			.withSeed(5678L)
			.withSteps(50)
			.withStylePreset("anime")
			.build();

		StabilityAiImageModel imageModel = new StabilityAiImageModel(stabilityAiApi, defaultOptions);

		StabilityAiImageOptions mergedOptions = imageModel.mergeOptions(runtimeOptions, defaultOptions);

		assertThat(mergedOptions).satisfies(options -> {
			// Verify that all options match the runtime values, not the defaults
			assertThat(options.getN()).isEqualTo(2);
			assertThat(options.getModel()).isEqualTo("runtime-model");
			assertThat(options.getWidth()).isEqualTo(1024);
			assertThat(options.getHeight()).isEqualTo(768);
			assertThat(options.getResponseFormat()).isEqualTo("application/json");
			assertThat(options.getCfgScale()).isEqualTo(14.0f);
			assertThat(options.getClipGuidancePreset()).isEqualTo("FAST_GREEN");
			assertThat(options.getSampler()).isEqualTo("DDPM");
			assertThat(options.getSeed()).isEqualTo(5678L);
			assertThat(options.getSteps()).isEqualTo(50);
			assertThat(options.getStylePreset()).isEqualTo("anime");
		});
	}

	@Test
	void shouldUseDefaultOptionsWhenRuntimeOptionsAreNull() {

		StabilityAiApi stabilityAiApi = mock(StabilityAiApi.class);
		StabilityAiImageOptions defaultOptions = StabilityAiImageOptions.builder()
			.withN(1)
			.withModel("default-model")
			.withCfgScale(7.0f)
			.build();

		StabilityAiImageModel imageModel = new StabilityAiImageModel(stabilityAiApi, defaultOptions);

		StabilityAiImageOptions mergedOptions = imageModel.mergeOptions(null, defaultOptions);

		assertThat(mergedOptions).satisfies(options -> {
			assertThat(options.getN()).isEqualTo(1);
			assertThat(options.getModel()).isEqualTo("default-model");
			assertThat(options.getCfgScale()).isEqualTo(7.0f);
		});
	}

	@Test
	void shouldHandleGenericImageOptionsCorrectly() {

		StabilityAiApi stabilityAiApi = mock(StabilityAiApi.class);
		StabilityAiImageOptions defaultOptions = StabilityAiImageOptions.builder()
			.withN(1)
			.withModel("default-model")
			.withWidth(512)
			.withCfgScale(7.0f)
			.build();

		// Create a non-StabilityAi ImageOptions implementation
		ImageOptions genericOptions = new ImageOptions() {
			@Override
			public Integer getN() {
				return 2;
			}

			@Override
			public String getModel() {
				return "generic-model";
			}

			@Override
			public Integer getWidth() {
				return 1024;
			}

			@Override
			public Integer getHeight() {
				return null;
			}

			@Override
			public String getResponseFormat() {
				return null;
			}

			@Override
			public String getStyle() {
				return null;
			}
		};

		StabilityAiImageModel imageModel = new StabilityAiImageModel(stabilityAiApi, defaultOptions);

		StabilityAiImageOptions mergedOptions = imageModel.mergeOptions(genericOptions, defaultOptions);

		// Generic options should override defaults
		assertThat(mergedOptions.getN()).isEqualTo(2);
		assertThat(mergedOptions.getModel()).isEqualTo("generic-model");
		assertThat(mergedOptions.getWidth()).isEqualTo(1024);

		// Stability-specific options should retain default values
		assertThat(mergedOptions.getCfgScale()).isEqualTo(7.0f);
	}

}