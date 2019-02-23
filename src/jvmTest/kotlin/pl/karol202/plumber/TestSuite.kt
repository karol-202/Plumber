package pl.karol202.plumber

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
		LateValTest::class,
		OutputTest::class,
		BiLayerTest::class,
		PipelineTest::class
)
class TestSuite