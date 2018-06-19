package com.bigeek.flink.batch.connectors.bitcoin;

import com.bigeek.flink.utils.Network;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.apache.flink.api.common.io.DefaultInputSplitAssigner;
import org.apache.flink.api.common.io.RichInputFormat;
import org.apache.flink.api.common.io.statistics.BaseStatistics;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.io.GenericInputSplit;
import org.apache.flink.core.io.InputSplitAssigner;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * InputSource implementation for Bitcoin.
 */
public class BitcoinInputSource extends RichInputFormat<Block, GenericInputSplit> {


	/**
	 * Logger.
	 */
	private Logger logger = LoggerFactory.getLogger(BitcoinInputSource.class);

	/**
	 * Bitcoin client.
	 */
	private transient BitcoinClient bitcoinClient;

	/**
	 * Start block.
	 */
	private Integer start;

	/**
	 * End block.
	 */
	private Integer end;

	/**
	 * Indicates if it is finish.
	 */
	private boolean reachedEnd = false;

	/**
	 * Indicates the block number.
	 */
	private Integer split;

	/**
	 * Network for bitcoin connection.
	 */
	private Network network;

	/**
	 * Port.
	 */
	private Integer port;

	/**
	 * Host .
	 */
	private String host = "localhost";

	/**
	 * Username.
	 */
	private String user;

	/**
	 * Password.
	 */
	private String password;

	/**
	 * Constructor .
	 *
	 * @param host
	 * @param port
	 * @param network
	 * @param user
	 * @param password
	 * @param start
	 * @param end
	 */
	public BitcoinInputSource(String host, Integer port, Network network, String user, String password, Integer start, Integer end) {
		this.start = start;
		this.end = end;
		this.host = host;
		this.port = port;
		this.network = network;
		this.user = user;
		this.password = password;
	}

	/**
	 * Default constructor.
	 */
	public BitcoinInputSource() {
	}


	@Override
	public void configure(Configuration parameters) {

		this.network = Network.valueOf(parameters.getString("bitcoinj.network", Network.MAIN_NET.name()));
		if (this.network.equals(Network.MAIN_NET)) {
			this.port = 8332;
		} else {
			this.port = 18332;
		}
		this.port = parameters.getInteger("bitcoinj.port", this.port);
		this.host = parameters.getString("bitcoinj.host", host);
		this.user = parameters.getString("bitcoinj.user", this.user);
		this.password = parameters.getString("bitcoinj.password", this.password);


		NetworkParameters params = MainNetParams.get();
		if (this.network.equals(Network.TEST_NET)) {
			params = TestNet3Params.get();
		} else if (this.network.equals(Network.REGTEST_NET)) {
			params = RegTestParams.get();
		} else if (this.network.equals(Network.MAIN_NET)) {
			params = MainNetParams.get();
		}

		bitcoinClient = new BitcoinClient(params,
				URI.create(this.host.concat(":").concat(this.port.toString())),
				this.user, this.password);

		if (start == null) {
			start = parameters.getInteger("bitcoinj.start", 0);
		}
		if (end == null) {
			int latest;
			try {
				latest = bitcoinClient.getBlockChainInfo().getBlocks();
			} catch (IOException e) {
				throw new RuntimeException(e);

			}
			end = parameters.getInteger("bitcoinj.end", latest);
		}

	}

	@Override
	public BaseStatistics getStatistics(BaseStatistics cachedStatistics) {
		return cachedStatistics;
	}

	@Override
	public GenericInputSplit[] createInputSplits(int minNumSplits) throws IOException {

		GenericInputSplit[] ret = new GenericInputSplit[(this.end - this.start) + 1];

		int startLocal = this.start;
		for (int i = 0; i <= ret.length && startLocal <= this.end; i++) {
			ret[i] = new GenericInputSplit(startLocal, ret.length);
			startLocal++;
		}
		return ret;
	}

	@Override
	public InputSplitAssigner getInputSplitAssigner(GenericInputSplit[] inputSplits) {
		return new DefaultInputSplitAssigner(inputSplits);
	}

	@Override
	public void open(GenericInputSplit split) {
		this.split = split.getSplitNumber();
		reachedEnd = false;
	}


	@Override
	public boolean reachedEnd() {
		return reachedEnd;
	}

	@Override
	public Block nextRecord(Block reuse) throws IOException {
		logger.info("Getting block {}", this.split);
		reuse = bitcoinClient.getBlock(this.split);
		logger.info("Block got {}", this.split);
		reachedEnd = true;
		return reuse;

	}

	@Override
	public void close() {

	}


}
