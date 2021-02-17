package com.airoha.android.lib.fota;

import com.airoha.android.lib.RaceCommand.constant.NvKeyId;
import com.airoha.android.lib.RaceCommand.constant.Recipient;
import com.airoha.android.lib.fota.actionEnum.DualActionEnum;
import com.airoha.android.lib.fota.actionEnum.SingleActionEnum;
import com.airoha.android.lib.fota.fotaSetting.FotaDualSettings;
import com.airoha.android.lib.fota.fotaSetting.FotaSingleSettings;
import com.airoha.android.lib.fota.fotaState.StageEnum;
import com.airoha.android.lib.fota.nvr.NvrBinParser;
import com.airoha.android.lib.fota.nvr.NvrDescriptor;
import com.airoha.android.lib.fota.partition.PartitionId;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.fota.stage.FotaStage_WriteNV;
import com.airoha.android.lib.fota.stage.StopReason;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStageDualReset;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_00_FotaStartRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_00_GetAvaDst;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_00_GetBatteryRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_00_GetFwInfoRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_00_GetVersionRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_00_QueryPartitionInfoRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_00_QueryStateRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_01_Lock_UnlockRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_01_StartTranscationRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_04_CheckIntegrityStorageRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_05_CommitRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_05_DetachResetRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_06_StopRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_11_DiffFlashPartitionEraseStorageRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_12_ProgramDiffFotaStorageRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_13_GetPartitionEraseStatusStorageRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_14_CompareFileSystemPartitionRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_14_ComparePartitionV2StorageRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_ReclaimNvkeyRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_SuspendDspRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_WriteNVRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_WriteStateRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.QueryPartitionInfo;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_FotaStart;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_GetAudioChannel;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_00_GetAudioChannelRelay;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_GetBattery;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_GetFwInfo;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_GetVersion;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_QueryPartitionInfo;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_QueryState;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_01_Lock_Unlock;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_01_StartTranscation;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_04_CheckIntegrityStorage;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_05_Commit;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_05_DetachReset;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_06_Stop;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_11_DiffFlashPartitionEraseStorage;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_12_ProgramDiffFotaStorage;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_13_GetPartitionEraseStatusStorage;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_14_CompareFileSystemPartition;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_14_ComparePartitionV2Storage;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_ReclaimNvkey;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_SuspendDsp;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_WriteState;
import com.airoha.android.lib.transport.AirohaLink;

import java.util.List;

import static com.airoha.android.lib.fota.stage.IAirohaFotaStage.SKIP_TYPE.All_stages;
import static com.airoha.android.lib.fota.stage.IAirohaFotaStage.SKIP_TYPE.CompareErase_stages;
import static com.airoha.android.lib.fota.stage.IAirohaFotaStage.SKIP_TYPE.Compare_stages;
import static com.airoha.android.lib.fota.stage.IAirohaFotaStage.SKIP_TYPE.Erase_stages;

/**
 * A class to handle single/dual device FOTA. Don't forget to register the callback {@link OnAirohaFotaStatusClientAppListener}.
 * <p>
 * The step of FOTA related operation roughly like below
 * <p>
 * 1. Create a {@link AirohaLink}
 * <p>
 * 2. implement its callback interface OnAirohaConnStateListener and register it in AirohaLink
 * <p>
 * 3. Create a Airoha153xMceRaceOtaMgr with the AirohaLink instance as parameter
 * <p>
 * 4. Implement the interface OnAirohaFotaStatusClientAppListener and register it in Airoha153xMceRaceOtaMgr by registerListener().
 * <p>
 * 5. Use AirohaLink to connect the target device
 * <p>
 * 6. When SPP is connected, use querySingleFotaInfo() or queryDualFotaInfo() to query device information
 * <p>
 * 7. Wait for the callback of onAvailableSingleActionUpdated() or onAvailableDualActionUpdated(), framework will tell you what's the next step.
 * <p>
 * 8. Use startSingleFota() or startDualFota() to start FOTA
 * <p>
 * 9. In FOTA processing, you can listen the callback onProgressUpdated() to update the FOTA progress bar.
 * <p>
 * 10. You also need to listen these callback for error handling: notifyError(), notifyStatus(), notifyInterrupted(), notifyStateEnum(), notifyBatterLevelLow().
 * <p>
 * 11. Once the FOTA is completed, single FOTA will call the callback notifyCompleted(), and dual FOTA will call the callback onAvailableDualActionUpdated() with parameter DualActionEnum.TwsCommit
 * <p>
 * 12. After the target device upgrade and reboot success, lib reconnect the device and call the querySingleFotaInfo() or queryDualFotaInfo() again
 * <p>
 * 13. When FOTA update is finished, the next available action should be FileSystemUpdate, then to call startSingleFota() or startDualFota() with actionEnum = RestoreNewFileSystem
 * <p>
 * 14. When FileSystemUpdate is success, device will reset in the end, and application reconnect the device and call the querySingleFotaInfo() or queryDualFotaInfo() to make sure all step are finished.
 * <br><br><br>
 * [SingleFOTA Flow]
 * <br>
 * querySingleFotaInfo() >> startSingleFota() for FOTA >> SPP reconnect >> querySingleFotaInfo() >> startSingleFota() for FileSystem >> SPP reconnect >> querySingleFotaInfo()
 * <br><br><br>
 * [MCSyncFOTA Flow]
 * <br>
 * queryDualFotaInfo() >> startDualFota() for FOTA >> SPP reconnect >> queryDualFotaInfo() >> startDualFota() for FileSystem >> SPP reconnect >> queryDualFotaInfo()
 * <br><br><br>
 *  Note: Commit/Reset command will change NVKEY 0x3A00 to 1,
 *  and then device will power on after press power/reset key (KEY event will be changed).
 *  So, application must to invoke querySingleFotaInfo() or queryDualFotaInfo() after SPP reconnected when FOTA or FileSystem update finished,
 *  querySingleFotaInfo() or queryDualFotaInfo() will change NVKEY 0x3A00 to 0.
 */
public class Airoha153xMceRaceOtaMgr extends AirohaRaceOtaMgr {
    private static final String TAG = "Airoha153xMceRaceOtaMgr";

    /**
     * FOTA manager constructor, need a connected AirohaLink instance.
     *
     * @param airohaLink
     */
    public Airoha153xMceRaceOtaMgr(AirohaLink airohaLink) {
        super(airohaLink);
    }

    /**
     * query agent's fota information.
     * Once the Client App is connected to the {@link AirohaLink} should call this API to check the state of FOTA flow
     */
    public void querySingleFotaInfo() {
        renewStageQueue();

        // 2018.01.07 [BTA-3177] - update the nv item at the head
        mStagesQueue.offer(new FotaStage_ReclaimNvkey(this, (short)1));
        mStagesQueue.offer(new FotaStage_WriteNV(this, NvKeyId.NV_RECONNECT, new byte[]{0x00}));

        QueryPartitionInfo[] queryPartitionInfos = createQueryFotaAndFilesystemPartitionInfos();
        mStagesQueue.offer(new FotaStage_00_QueryPartitionInfo(this, queryPartitionInfos));

        byte[] recipients = new byte[]{Recipient.DontCare};
        mStagesQueue.offer(new FotaStage_00_GetVersion(this, recipients));
        mStagesQueue.offer(new FotaStage_00_QueryState(this, recipients));

        // 2018.01.07 [BTA-3177] - query the battery at the tail
        mStagesQueue.offer(new FotaStage_00_GetBattery(this));

        startPollStagetQueue();
    }

    /**
     * To query the specified role's fota information.
     *
     * @param role
     */
    public void querySingleFotaInfo(byte role) {
        renewStageQueue();

        if(role == AgentClientEnum.AGENT){
            // 2018.01.07 [BTA-3177] - update the nv item at the head
            mStagesQueue.offer(new FotaStage_ReclaimNvkey(this, (short)1));
            mStagesQueue.offer(new FotaStage_WriteNV(this, NvKeyId.NV_RECONNECT, new byte[]{0x00}));

            QueryPartitionInfo[] queryPartitionInfos = createQueryFotaAndFilesystemPartitionInfos();
            mStagesQueue.offer(new FotaStage_00_QueryPartitionInfo(this, queryPartitionInfos));

            byte[] recipients = new byte[]{Recipient.DontCare};
            mStagesQueue.offer(new FotaStage_00_GetVersion(this, recipients));
            mStagesQueue.offer(new FotaStage_00_GetFwInfo(this, recipients));
            mStagesQueue.offer(new FotaStage_00_QueryState(this, recipients));

            // 2018.01.07 [BTA-3177] - query the battery at the tail
            mStagesQueue.offer(new FotaStage_00_GetBattery(this));
        }

        if(role == AgentClientEnum.CLIENT){
            mStagesQueue.offer(new FotaStage_00_GetAvaDst(this));

            mStagesQueue.offer(new FotaStage_ReclaimNvkeyRelay(this, (short)1));
            mStagesQueue.offer(new FotaStage_WriteNVRelay(this, NvKeyId.NV_RECONNECT, new byte[]{0x00}));

            QueryPartitionInfo[] queryPartitionInfos = createQueryFotaAndFilesystemPartitionInfos();
            mStagesQueue.offer(new FotaStage_00_QueryPartitionInfoRelay(this, queryPartitionInfos));

            byte[] recipients = new byte[]{Recipient.DontCare};
            mStagesQueue.offer(new FotaStage_00_GetVersionRelay(this, recipients));
            mStagesQueue.offer(new FotaStage_00_GetFwInfoRelay(this, recipients));
            mStagesQueue.offer(new FotaStage_00_QueryStateRelay(this, recipients));

            // 2018.01.07 [BTA-3177] - query the battery at the tail
            mStagesQueue.offer(new FotaStage_00_GetBatteryRelay(this));
        }

        startPollStagetQueue();
    }

    /**
     * To set battery threshold of FOTA start/commit and then to query the specified role's fota information.
     *
     * @param role
     * @param batteryThreshold
     */
    public void querySingleFotaInfo(byte role, int batteryThreshold) {
        /// should not save into the FotaDualSettings, but it is used in FotaStage_00_GetBattery
        mFotaDualSettings.batteryThreshold = batteryThreshold;
        querySingleFotaInfo(role);
    }

    /**
     * To set battery threshold of FOTA start/commit and then to query dual fota information.
     *
     * @param batteryThreshold
     */
    public void queryDualFotaInfo(int batteryThreshold) {
        mFotaDualSettings.batteryThreshold = batteryThreshold;
        queryDualFotaInfo();
    }

    /**
     * query dual fota information.
     * Once the Client App is connected to the {@link AirohaLink} should call this API to check the state of FOTA flow
     *
     */
    public void queryDualFotaInfo() {
        renewStageQueue();

        mStagesQueue.offer(new FotaStage_00_GetAvaDst(this));

        // 2018.01.07 [BTA-3177] - update the nv item at the head
        mStagesQueue.offer(new FotaStage_ReclaimNvkey(this, (short) 1));
        mStagesQueue.offer(new FotaStage_WriteNV(this, NvKeyId.NV_RECONNECT, new byte[]{0x00}));
        mStagesQueue.offer(new FotaStage_ReclaimNvkeyRelay(this, (short)1));
        mStagesQueue.offer(new FotaStage_WriteNVRelay(this, NvKeyId.NV_RECONNECT, new byte[]{0x00}));

        QueryPartitionInfo[] queryPartitionInfos = createQueryFotaAndFilesystemPartitionInfos();
        mStagesQueue.offer(new FotaStage_00_QueryPartitionInfo(this, queryPartitionInfos));
        mStagesQueue.offer(new FotaStage_00_QueryPartitionInfoRelay(this, queryPartitionInfos));

        byte[] recipients = new byte[]{Recipient.DontCare};

        mStagesQueue.offer(new FotaStage_00_GetVersion(this, recipients));
        mStagesQueue.offer(new FotaStage_00_GetVersionRelay(this, recipients));

        mStagesQueue.offer(new FotaStage_00_GetFwInfo(this, recipients));
        mStagesQueue.offer(new FotaStage_00_GetFwInfoRelay(this, recipients));

        mStagesQueue.offer(new FotaStage_00_GetAudioChannel(this));
        mStagesQueue.offer(new FotaStage_00_GetAudioChannelRelay(this));

        mStagesQueue.offer(new FotaStage_00_QueryState(this, recipients));
        mStagesQueue.offer(new FotaStage_00_QueryStateRelay(this, recipients));

        // 2018.01.07 [BTA-3177] - query the battery at the tail
        mStagesQueue.offer(new FotaStage_00_GetBattery(this));
        mStagesQueue.offer(new FotaStage_00_GetBatteryRelay(this));

        startPollStagetQueue();
    }

    /**
     * start single fota.
     */
    public void startSingleFota(String filePath, FotaSingleSettings settings, byte role) {

        FotaStage.setDelayPollTime(settings.programInterval);
        FotaStage.setPrePollSize(settings.slidingWindow);

        /// should not save into the FotaDualSettings, but it is used in FotaStage_00_GetBattery
        mFotaDualSettings.batteryThreshold = settings.batteryThreshold;

        if (settings.actionEnum == SingleActionEnum.StartFota) {
            startUpdateSingleFotaPartition(filePath, role);
            return;
        }

        if (settings.actionEnum == SingleActionEnum.RestoreNewFileSystem) {
            startUpdateSingleFileSystemPartition(filePath, true, role);
            return;
        }

//        if (settings.actionEnum == SingleActionEnum.RestoreOldFileSystem) {
//            startUpdateSingleFileSystemPartition(filePath, false, role);
//            return;
//        }

        if(settings.actionEnum == SingleActionEnum.UpdateNvr) {
            startUpdateSingleNvr(filePath, role);
            return;
        }
    }

    /**
     * start dual fota.
     */
    @Override
    public void startDualFota(String agentFilePath, String partnerFilePath, FotaDualSettings settings) throws IllegalArgumentException {
        FotaStage.setDelayPollTime(settings.programInterval);
        FotaStage.setPrePollSize(settings.slidingWindow);

        mFotaDualSettings.batteryThreshold = settings.batteryThreshold;

        if (settings.actionEnum == DualActionEnum.StartFota) {
            startUpdateDualFotaPartition(agentFilePath, partnerFilePath);
            return;
        }

        if (settings.actionEnum == DualActionEnum.RestoreNewFileSystem) {
            startUpdateDualFileSystemPartition(agentFilePath, true);
            return;
        }

//        if (settings.actionEnum == DualActionEnum.RestoreOldFileSystem) {
//            startUpdateDualFileSystemPartition(agentFilePath, false);
//            return;
//        }

        if(settings.actionEnum == DualActionEnum.UpdateNvr) {
            startUpdateDualNvr(agentFilePath, partnerFilePath);
            return;
        }
    }

    /**
     * check the uploaded FOTA bin is valid or not. (for debugging)
     */
    public void testDualIntegrityCheck() {
        renewStageQueue();

        mStagesQueue.offer(new FotaStage_04_CheckIntegrityStorage(this));
        mStagesQueue.offer(new FotaStage_WriteState(this, StageEnum.APP_TWS_INTEGRITY_CHECK_SUCCESS));

        mStagesQueue.offer(new FotaStage_04_CheckIntegrityStorageRelay(this));
        mStagesQueue.offer(new FotaStage_WriteStateRelay(this, StageEnum.APP_TWS_INTEGRITY_CHECK_SUCCESS));

        startPollStagetQueue();
    }

//==========================================================================================

    private void configAgentReady() {
        mStagesQueue.offer(new FotaStage_00_GetBattery(this));
        // 2018.11.28 remove SwitchPowerMode
//        mStagesQueue.offer(new FotaStage_SwitchPowerMode(this, ModeEnum.NORMAL));
        // 2018.11.28 SuspendDsp first
        mStagesQueue.offer(new FotaStage_SuspendDsp(this));
        // 2018.11.28 Call Start after Suspend. Mandatory!
    }

    private void configPartnerReady() {
        mStagesQueue.offer(new FotaStage_00_GetAvaDst(this));
        mStagesQueue.offer(new FotaStage_00_GetBatteryRelay(this));
        // 2018.11.28 remove SwitchPowerMode
//        mStagesQueue.offer(new FotaStage_SwitchPowerModeRelay(this, ModeEnum.NORMAL));
        // 2018.11.28 SuspendDsp first
        mStagesQueue.offer(new FotaStage_SuspendDspRelay(this));
        // 2018.11.28 Call Start after Suspend. Mandatory!
    }

    private void configUpdateAgentFileSystemStages() {
        //boolean isNewFileSystem, boolean isDualMode) {
        // 0x1C00 - 10/26 revised
        mStagesQueue.offer(new FotaStage_00_QueryPartitionInfo(this, createQueryFileSystemPartitionInfo()));

        // stages may be skipped
        FotaStage getPartitionEraseStatus = new FotaStage_13_GetPartitionEraseStatusStorage(this, this.getFotaFileSystemInputStream());
        FotaStage comparePartitionV2 = new FotaStage_14_ComparePartitionV2Storage(this);
        FotaStage unlock = new FotaStage_01_Lock_Unlock(this, false);
        FotaStage diffFlashPartitionErase = new FotaStage_11_DiffFlashPartitionEraseStorage(this);
        FotaStage programDiffFota = new FotaStage_12_ProgramDiffFotaStorage(this);

        getPartitionEraseStatus.addStageForPartialSkip(Compare_stages, comparePartitionV2);
        getPartitionEraseStatus.addStageForPartialSkip(CompareErase_stages, comparePartitionV2);
        getPartitionEraseStatus.addStageForPartialSkip(CompareErase_stages, diffFlashPartitionErase);

        comparePartitionV2.addStageForPartialSkip(Erase_stages, diffFlashPartitionErase);

        comparePartitionV2.addStageForPartialSkip(All_stages, diffFlashPartitionErase);
        comparePartitionV2.addStageForPartialSkip(All_stages, programDiffFota);

        byte[] recipients = new byte[]{Recipient.DontCare};

        // 0x1C08 - 2018.12.21 BTA-3022
        mStagesQueue.offer(new FotaStage_00_FotaStart(this, recipients));
        // 0x0433
        mStagesQueue.offer(getPartitionEraseStatus);
        // 0x0431
        mStagesQueue.offer(comparePartitionV2);
        // 0x0430
        mStagesQueue.offer(unlock);
        // 0x0404
        mStagesQueue.offer(diffFlashPartitionErase);
        // 0x0402
        mStagesQueue.offer(programDiffFota);

        mStagesQueue.offer(new FotaStage_06_Stop(this, recipients, StopReason.ActiveFotaStopped));

        // 0x0431
        mStagesQueue.offer(new FotaStage_14_CompareFileSystemPartition(this, this.mFotaFileSystemBinFile));

//        if(isDualMode){
//            if(isNewFileSystem){
//                mStagesQueue.offer(new FotaStage_WriteState(this, StageEnum.APP_TWS_NEW_FILE_SYSTEM_UPDATE_COMPLETE)); // 0322
//            }else {
//                mStagesQueue.offer(new FotaStage_WriteState(this, StageEnum.APP_TWS_RESULT_FOTA_FAIL_FILE_SYSTEM_RESTORE)); // 0342
//            }
//        }else {
//            if (isNewFileSystem) {
//                mStagesQueue.offer(new FotaStage_WriteState(this, StageEnum.APP_NEW_FILE_SYSTEM_UPDATE_COMPLETE)); // 0222
//            } else {
//                mStagesQueue.offer(new FotaStage_WriteState(this, StageEnum.APP_RESULT_FOTA_FAIL_FILE_SYSTEM_RESTORE)); // 0242
//            }
//        }
        mStagesQueue.offer(new FotaStage_01_StartTranscation(this));

    }

    private void configUpdateAgentFotaPartitionStages(boolean isDualMode) {
        mStagesQueue.offer(new FotaStage_00_QueryPartitionInfo(this, createQueryFotaPartitionInfo()));
        // stages may be skipped
        FotaStage getPartitionEraseStatus = new FotaStage_13_GetPartitionEraseStatusStorage(this, this.getFotaAgentInputStream());
        FotaStage comparePartitionV2 = new FotaStage_14_ComparePartitionV2Storage(this);
        FotaStage unlock = new FotaStage_01_Lock_Unlock(this, false);
        FotaStage startTransaction = new FotaStage_01_StartTranscation(this);

        FotaStage diffFlashPartitionErase = new FotaStage_11_DiffFlashPartitionEraseStorage(this);

        FotaStage programDiffFota = new FotaStage_12_ProgramDiffFotaStorage(this);
        FotaStage checkIntegrity = new FotaStage_04_CheckIntegrityStorage(this);

        FotaStage writeState_integrityCheckSuccess;

        if(isDualMode){
            writeState_integrityCheckSuccess = new FotaStage_WriteState(this, StageEnum.APP_TWS_INTEGRITY_CHECK_SUCCESS);
        }else {
            writeState_integrityCheckSuccess = new FotaStage_WriteState(this, StageEnum.APP_INTEGRITY_CHECK_SUCCESS);
        }

        getPartitionEraseStatus.addStageForPartialSkip(Compare_stages, comparePartitionV2);
        getPartitionEraseStatus.addStageForPartialSkip(CompareErase_stages, comparePartitionV2);
        getPartitionEraseStatus.addStageForPartialSkip(CompareErase_stages, diffFlashPartitionErase);

        comparePartitionV2.addStageForPartialSkip(Erase_stages, diffFlashPartitionErase);

        comparePartitionV2.addStageForPartialSkip(All_stages, diffFlashPartitionErase);
        comparePartitionV2.addStageForPartialSkip(All_stages, programDiffFota);

        byte[] recipients = new byte[]{Recipient.DontCare};

        mStagesQueue.offer(new FotaStage_00_FotaStart(this, recipients));

        mStagesQueue.offer(startTransaction);
        // 0x0433
        mStagesQueue.offer(getPartitionEraseStatus);
        // 0x0431
        mStagesQueue.offer(comparePartitionV2);
        // 0x0430
        mStagesQueue.offer(unlock);

        mStagesQueue.offer(diffFlashPartitionErase);

        mStagesQueue.offer(programDiffFota);

        mStagesQueue.offer(new FotaStage_06_Stop(this, recipients, StopReason.ActiveFotaStopped));
        // 0x1C01
        mStagesQueue.offer(checkIntegrity);
        // 0x1C06
        mStagesQueue.offer(writeState_integrityCheckSuccess);
    }

    protected void configUpdatePartnerFileSystemStages() {
        //boolean isNewFileSystem, boolean isDualMode) {
        // 0x1C00 - 10/26 revised
        mStagesQueue.offer(new FotaStage_00_QueryPartitionInfoRelay(this, createQueryFileSystemPartitionInfo()));

        // stages may be skipped
        FotaStage getPartitionEraseStatus = new FotaStage_13_GetPartitionEraseStatusStorageRelay(this, this.getFotaFileSystemInputStream());
        FotaStage comparePartitionV2 = new FotaStage_14_ComparePartitionV2StorageRelay(this);
        FotaStage unlock = new FotaStage_01_Lock_UnlockRelay(this, false);
        FotaStage diffFlashPartitionErase = new FotaStage_11_DiffFlashPartitionEraseStorageRelay(this);
        FotaStage programDiffFota = new FotaStage_12_ProgramDiffFotaStorageRelay(this);

        getPartitionEraseStatus.addStageForPartialSkip(Compare_stages, comparePartitionV2);
        getPartitionEraseStatus.addStageForPartialSkip(CompareErase_stages, comparePartitionV2);
        getPartitionEraseStatus.addStageForPartialSkip(CompareErase_stages, diffFlashPartitionErase);

        comparePartitionV2.addStageForPartialSkip(Erase_stages, diffFlashPartitionErase);

        comparePartitionV2.addStageForPartialSkip(All_stages, diffFlashPartitionErase);
        comparePartitionV2.addStageForPartialSkip(All_stages, programDiffFota);

        byte[] recipients = new byte[]{Recipient.DontCare};

        // 0x1C08 - 2018.12.21 BTA-3022
        mStagesQueue.offer(new FotaStage_00_FotaStartRelay(this, recipients));
        // 0x0433
        mStagesQueue.offer(getPartitionEraseStatus);
        // 0x0431
        mStagesQueue.offer(comparePartitionV2);
        // 0x0430
        mStagesQueue.offer(unlock);
        // 0x0404
        mStagesQueue.offer(diffFlashPartitionErase);
        // 0x0402
        mStagesQueue.offer(programDiffFota);

        mStagesQueue.offer(new FotaStage_06_StopRelay(this, recipients, StopReason.ActiveFotaStopped));
        // 0x0431
        mStagesQueue.offer(new FotaStage_14_CompareFileSystemPartitionRelay(this, this.mFotaFileSystemBinFile));

//        if (isNewFileSystem) {
//            // 0x1C06
//            mStagesQueue.offer(new FotaStage_WriteStateRelay(this, StageEnum.APP_TWS_NEW_FILE_SYSTEM_UPDATE_COMPLETE)); // 0322
//        } else {
//            mStagesQueue.offer(new FotaStage_WriteStateRelay(this, StageEnum.APP_TWS_RESULT_FOTA_FAIL_FILE_SYSTEM_RESTORE)); // 0342
//        }

//        if(isDualMode){
//            if(isNewFileSystem){
//                mStagesQueue.offer(new FotaStage_WriteStateRelay(this, StageEnum.APP_TWS_NEW_FILE_SYSTEM_UPDATE_COMPLETE)); // 0322
//            }else {
//                mStagesQueue.offer(new FotaStage_WriteStateRelay(this, StageEnum.APP_TWS_RESULT_FOTA_FAIL_FILE_SYSTEM_RESTORE)); // 0342
//            }
//        }else {
//            if (isNewFileSystem) {
//                mStagesQueue.offer(new FotaStage_WriteStateRelay(this, StageEnum.APP_NEW_FILE_SYSTEM_UPDATE_COMPLETE)); // 0222
//            } else {
//                mStagesQueue.offer(new FotaStage_WriteStateRelay(this, StageEnum.APP_RESULT_FOTA_FAIL_FILE_SYSTEM_RESTORE)); // 0242
//            }
//        }

        mStagesQueue.offer(new FotaStage_01_StartTranscationRelay(this));
    }

    protected void configUpdatePartnerFotaPartitionStages() {
        mStagesQueue.offer(new FotaStage_00_QueryPartitionInfoRelay(this, createQueryFotaPartitionInfo()));
        FotaStage getPartitionEraseStatus = new FotaStage_13_GetPartitionEraseStatusStorageRelay(this, this.getFotaPartnerInputStream());
        FotaStage comparePartitionV2 = new FotaStage_14_ComparePartitionV2StorageRelay(this);
        FotaStage unlock = new FotaStage_01_Lock_UnlockRelay(this, false);
        FotaStage startTransaction = new FotaStage_01_StartTranscationRelay(this);
        FotaStage diffFlashPartitionErase = new FotaStage_11_DiffFlashPartitionEraseStorageRelay(this);
        FotaStage programDiffFota = new FotaStage_12_ProgramDiffFotaStorageRelay(this);
        FotaStage checkIntegrity = new FotaStage_04_CheckIntegrityStorageRelay(this);
        FotaStage writeState_integrityCheckSuccess = new FotaStage_WriteStateRelay(this, StageEnum.APP_TWS_INTEGRITY_CHECK_SUCCESS);


        getPartitionEraseStatus.addStageForPartialSkip(Compare_stages, comparePartitionV2);
        getPartitionEraseStatus.addStageForPartialSkip(CompareErase_stages, comparePartitionV2);
        getPartitionEraseStatus.addStageForPartialSkip(CompareErase_stages, diffFlashPartitionErase);

        comparePartitionV2.addStageForPartialSkip(Erase_stages, diffFlashPartitionErase);

        comparePartitionV2.addStageForPartialSkip(All_stages, diffFlashPartitionErase);
        comparePartitionV2.addStageForPartialSkip(All_stages, programDiffFota);

        byte[] recipients = new byte[]{Recipient.DontCare};

        // 0x1C08 - 2018.12.21 BTA-3022
        mStagesQueue.offer(new FotaStage_00_FotaStartRelay(this, recipients));
        // 0x1C0A
        mStagesQueue.offer(startTransaction);
        // 0x0433
        mStagesQueue.offer(getPartitionEraseStatus);
        // 0x0431
        mStagesQueue.offer(comparePartitionV2);
        // 0x0430
        mStagesQueue.offer(unlock);
        // 0x0404
        mStagesQueue.offer(diffFlashPartitionErase);

        mStagesQueue.offer(programDiffFota);

        mStagesQueue.offer(new FotaStage_06_StopRelay(this, recipients, StopReason.ActiveFotaStopped));
        // 0x1C01
        mStagesQueue.offer(checkIntegrity);
        // 0x1C06
        mStagesQueue.offer(writeState_integrityCheckSuccess);
    }

    private QueryPartitionInfo[] createQueryFileSystemPartitionInfo() {
        QueryPartitionInfo[] queryPartitionInfos = new QueryPartitionInfo[1];
        queryPartitionInfos[0] = new QueryPartitionInfo(Recipient.DontCare, PartitionId.FILE_SYSTEM);
        return queryPartitionInfos;
    }

    private QueryPartitionInfo[] createQueryFotaAndFilesystemPartitionInfos() {
        QueryPartitionInfo[] queryPartitionInfos = new QueryPartitionInfo[2];
        queryPartitionInfos[0] = new QueryPartitionInfo(Recipient.DontCare, PartitionId.FOTA);
        queryPartitionInfos[1] = new QueryPartitionInfo(Recipient.DontCare, PartitionId.FILE_SYSTEM);
        return queryPartitionInfos;
    }

    private QueryPartitionInfo[] createQueryFotaPartitionInfo() {
        QueryPartitionInfo[] queryPartitionInfos = new QueryPartitionInfo[1];
        queryPartitionInfos[0] = new QueryPartitionInfo(Recipient.DontCare, PartitionId.FOTA);
        return queryPartitionInfos;
    }

    @Override
    protected void handleQueriedStates(int queryState) {
        if (mIsNeedToUpdateFileSystem) {
            switch (queryState) {
//                case StageEnum.APP_FILE_SYSTEM_NEED_RESTORE:
//                    // Ask restart Restore Old FileSystem ?
//                    notifySingleAction(SingleActionEnum.RestoreOldFileSystem);
//                    break;
                case StageEnum.APP_UNKNOWN:
                    notifySingleAction(SingleActionEnum.StartFota);
                    break;

//                case StageEnum.APP_INTEGRITY_CHECK_SUCCESS: // 2018.05.24 BTA-1508
//                case StageEnum.APP_RESULT_FOTA_FAIL_FILE_SYSTEM_RESTORE:
//                    // 2018.08.03 fix side effect after restore old file system, need to update NvKey
//                    notifySingleAction(SingleActionEnum.NeedToUpdateReconnectNvKey);
//                case StageEnum.APP_ERASE_START:
//                    // User Trigger FOTA
//                    notifySingleAction(SingleActionEnum.StartFota);
//                    notifySingleAction(SingleActionEnum.RestoreOldFileSystem);
//                    break;
                case StageEnum.FW_MOVE_WORKING_AREA_COMPLETE:
                    // Ask restart Restore New FileSystem ?
                    notifySingleAction(SingleActionEnum.StartFota);
                    notifySingleAction(SingleActionEnum.RestoreNewFileSystem);
                    break;
//                case StageEnum.APP_NEW_FILE_SYSTEM_UPDATE_COMPLETE:
////                    updateReconnectNvKeySingle();
//                    notifySingleAction(SingleActionEnum.StartFota);
//                    notifySingleAction(SingleActionEnum.UpdateNvr);
//                    break;
//                case StageEnum.APP_RESULT_FOTA_FILE_SYSTEM_NVKEY_SUCCESS:
////                    updateReconnectNvKeySingle();
//                    notifySingleAction(SingleActionEnum.StartFota);
//                    break;
                default:
                    notifySingleAction(SingleActionEnum.StartFota);
                    break;
            }
        }
    }

    @Override
    protected void handleTwsQueriedStates() {
        // TODO remove for release
        if (mIsFlashOperationAllowed) {
            notifyDualAction(DualActionEnum.StartFota);
        }

        if(mAgentFotaState == StageEnum.FW_MOVE_WORKING_AREA_COMPLETE && mPartnerFotaState == StageEnum.FW_MOVE_WORKING_AREA_COMPLETE){
            notifyDualAction(DualActionEnum.RestoreNewFileSystem);
            return;
        }

        // 2018.12.21 BTA-3021
//        if(mAgentFotaState == StageEnum.APP_TWS_NEW_FILE_SYSTEM_UPDATE_COMPLETE
//                && mPartnerFotaState == StageEnum.FW_MOVE_WORKING_AREA_COMPLETE) {
//
//            notifyDualAction(DualActionEnum.RestoreNewFileSystem);
//
//            return;
//        }

        if (mAgentFotaState == StageEnum.FW_MOVE_WORKING_AREA_COMPLETE ||
                mPartnerFotaState == StageEnum.FW_MOVE_WORKING_AREA_COMPLETE ) {
            notifyDualAction(DualActionEnum.StartFota);
            return;
        }

//        if (mAgentFotaState == StageEnum.APP_TWS_RESULT_FOTA_NV_SUCCESS &&
//                (mPartnerFotaState == StageEnum.FW_MOVE_WORKING_AREA_COMPLETE
//                        || mPartnerFotaState == StageEnum.APP_TWS_START_UPDTAE_NV_KEY)) {
//
//            notifyDualAction(DualActionEnum.RoleSwitch);
//
//            return;
//        }

//        if (mAgentFotaState == StageEnum.FW_MOVE_WORKING_AREA_COMPLETE &&
//                (mPartnerFotaState == StageEnum.FW_MOVE_WORKING_AREA_COMPLETE ||
//                        mPartnerFotaState == StageEnum.APP_TWS_START_UPDTAE_NV_KEY ||
//                        mPartnerFotaState == StageEnum.APP_TWS_RESULT_FOTA_NV_SUCCESS)) {
//
//            notifyDualAction(DualActionEnum.StartNvKeyUpdate);
//
//            return;
//        }

//        if (mAgentFotaState == StageEnum.APP_TWS_START_UPDTAE_NV_KEY &&
//                (mPartnerFotaState == StageEnum.FW_MOVE_WORKING_AREA_COMPLETE ||
//                        mPartnerFotaState == StageEnum.APP_TWS_START_UPDTAE_NV_KEY ||
//                        mPartnerFotaState == StageEnum.APP_TWS_RESULT_FOTA_NV_SUCCESS)) {
//
//            notifyDualAction(DualActionEnum.StartNvKeyUpdate);
//
//            return;
//        }

        if (mAgentFotaState == StageEnum.APP_TWS_INTEGRITY_CHECK_SUCCESS &&
                mPartnerFotaState == StageEnum.APP_TWS_INTEGRITY_CHECK_SUCCESS) {

            notifyDualAction(DualActionEnum.TwsCommit);
            dualCommit();
            return;
        }

//        if(mAgentFotaState == StageEnum.APP_TWS_NEW_FILE_SYSTEM_UPDATE_COMPLETE
//                && mPartnerFotaState == StageEnum.APP_TWS_NEW_FILE_SYSTEM_UPDATE_COMPLETE) {
////            notifyDualAction(DualActionEnum.TwsDetachReset);
////            notifyDualAction(DualActionEnum.StartFota);
////            dualSoftReset();
////            updateReconnectNvKeyDual();
//            notifyDualAction(DualActionEnum.UpdateNvr);
//            notifyDualAction(DualActionEnum.StartFota);
//
//            return;
//        }

//        if(mAgentFotaState == StageEnum.APP_TWS_RESULT_FOTA_FAIL_FILE_SYSTEM_RESTORE
//                && mPartnerFotaState == StageEnum.APP_TWS_RESULT_FOTA_FAIL_FILE_SYSTEM_RESTORE) {
//            notifyDualAction(DualActionEnum.TwsDetachReset);
//            notifyDualAction(DualActionEnum.StartFota);
////            dualSoftReset();
//
//            return;
//        }

//        if(mAgentFotaState == StageEnum.APP_TWS_RESULT_FOTA_FILE_SYSTEM_NVKEY_SUCCESS
//                && mPartnerFotaState == StageEnum.APP_TWS_RESULT_FOTA_FILE_SYSTEM_NVKEY_SUCCESS) {
////            updateReconnectNvKeyDual();
//            notifyDualAction(DualActionEnum.StartFota);
//            return;
//        }


        // other case
        if (mIsFlashOperationAllowed) {
            notifyDualAction(DualActionEnum.StartFota);
        }
    }


    private void startUpdateDualFileSystemPartition(String filePath, boolean isNewFileSystem) {
        setInputFile(filePath, 2);

        renewStageQueue();

        configAgentReady();
        configPartnerReady();

        configUpdateAgentFileSystemStages();//isNewFileSystem, true);
        configUpdatePartnerFileSystemStages();//isNewFileSystem, true);

        mStagesQueue.offer(new FotaStageDualReset(this));

        startPollStagetQueue();
    }

    private void startUpdateDualFotaPartition(String agentFilePath, String partnerFilePath) {

        renewStageQueue();

        configAgentReady();
        configPartnerReady();

        setInputFile(agentFilePath, 0);
        configUpdateAgentFotaPartitionStages(true);

        setInputFile(partnerFilePath, 1);
        configUpdatePartnerFotaPartitionStages();

        mStagesQueue.offer(new FotaStage_00_GetBatteryRelay(this));
        mStagesQueue.offer(new FotaStage_00_GetBattery(this));

        mStagesQueue.offer(new FotaStage_05_CommitRelay(this));
        mStagesQueue.offer(new FotaStage_05_Commit(this));

        startPollStagetQueue();
    }

    private void startUpdateSingleFileSystemPartition(String filePath, boolean isNewFileSystem, byte role) {
        setInputFile(filePath, 2);

        renewStageQueue();

        if(role == AgentClientEnum.AGENT) {
            // stages can't be skipped
            configAgentReady();
            configUpdateAgentFileSystemStages();//isNewFileSystem, false);
            // 0x1C05
            mStagesQueue.offer(new FotaStage_05_DetachReset(this));
        }

        if(role == AgentClientEnum.CLIENT) {
            configPartnerReady();
            configUpdatePartnerFileSystemStages();//isNewFileSystem, false);

            mStagesQueue.offer(new FotaStage_05_DetachResetRelay(this));
        }
        startPollStagetQueue();
    }

    private void startUpdateSingleFotaPartition(String filePath, byte role) {
        setInputFile(filePath, role);

        renewStageQueue();

        if(role == AgentClientEnum.AGENT){
            configAgentReady();
            configUpdateAgentFotaPartitionStages(false);

            mStagesQueue.offer(new FotaStage_00_GetBattery(this));
            mStagesQueue.offer(new FotaStage_05_Commit(this));
        }

        if(role == AgentClientEnum.CLIENT){
           configPartnerReady();
           configUpdatePartnerFotaPartitionStages();

           mStagesQueue.offer(new FotaStage_00_GetBatteryRelay(this));
           mStagesQueue.offer(new FotaStage_05_CommitRelay(this));
        }

        startPollStagetQueue();
    }

    private void dualCommit(){

        renewStageQueue();

        mStagesQueue.offer(new FotaStage_00_GetBatteryRelay(this));
        mStagesQueue.offer(new FotaStage_00_GetBattery(this));

        mStagesQueue.offer(new FotaStage_05_CommitRelay(this));
        mStagesQueue.offer(new FotaStage_05_Commit(this));

        startPollStagetQueue();
    }

    private void startUpdateSingleNvr(String nvrBinFilePath, byte role){
        if (nvrBinFilePath == null || nvrBinFilePath.isEmpty())
            return;

        renewStageQueue();

        NvrBinParser nvrBinParser = new NvrBinParser(nvrBinFilePath);
        nvrBinParser.startParse();
        List<NvrDescriptor> nvrDescriptorList = nvrBinParser.getListNvrDescriptor();
        if (nvrDescriptorList != null) {

            /// 2019.05.28  SuspendDsp first
            mStagesQueue.offer(new FotaStage_SuspendDsp(this));

            for (NvrDescriptor nvrDescriptor : nvrDescriptorList) {

                if(role == AgentClientEnum.AGENT) {
                    mStagesQueue.offer(new FotaStage_ReclaimNvkey(this, (short) nvrDescriptor.getNvValue().length));

                    mStagesQueue.offer(
                            new FotaStage_WriteNV(this,
                                    nvrDescriptor.getNvKey(), nvrDescriptor.getNvValue()));
                }

                if(role == AgentClientEnum.CLIENT) {
                    mStagesQueue.offer(new FotaStage_ReclaimNvkeyRelay(this, (short) nvrDescriptor.getNvValue().length));

                    mStagesQueue.offer(
                            new FotaStage_WriteNVRelay(this,
                                    nvrDescriptor.getNvKey(), nvrDescriptor.getNvValue()));
                }
            }

//            mStagesQueue.offer( new FotaStage_ResumeDsp(this));
            mStagesQueue.offer( new FotaStage_05_DetachReset(this));
        }

        startPollStagetQueue();
    }

    private void startUpdateDualNvr(String agentFilePath, String partnerFilePath){
        if (agentFilePath == null || agentFilePath.isEmpty() || partnerFilePath == null || partnerFilePath.isEmpty())
            return;

        renewStageQueue();

        mStagesQueue.offer(new FotaStage_00_GetAvaDst(this));

        NvrBinParser nvrBinParser = new NvrBinParser(agentFilePath);
        nvrBinParser.startParse();
        List<NvrDescriptor> nvrDescriptorList = nvrBinParser.getListNvrDescriptor();
        if (nvrDescriptorList != null) {
            /// 2019.05.28  SuspendDsp first
            mStagesQueue.offer(new FotaStage_SuspendDsp(this));

            for (NvrDescriptor nvrDescriptor : nvrDescriptorList) {
                mStagesQueue.offer(new FotaStage_ReclaimNvkey(this, (short) nvrDescriptor.getNvValue().length));

                mStagesQueue.offer(
                        new FotaStage_WriteNV(this,
                                nvrDescriptor.getNvKey(), nvrDescriptor.getNvValue()));
            }
        }

        nvrBinParser = new NvrBinParser(partnerFilePath);
        nvrBinParser.startParse();
        List<NvrDescriptor>  nvrDescriptorList_relay = nvrBinParser.getListNvrDescriptor();
        if (nvrDescriptorList_relay != null) {
            /// 2019.05.28  SuspendDsp first
            mStagesQueue.offer(new FotaStage_SuspendDspRelay(this));

            for (NvrDescriptor nvrDescriptor : nvrDescriptorList_relay) {
                mStagesQueue.offer(new FotaStage_ReclaimNvkeyRelay(this, (short) nvrDescriptor.getNvValue().length));

                mStagesQueue.offer(
                        new FotaStage_WriteNVRelay(this, nvrDescriptor.getNvKey(), nvrDescriptor.getNvValue()));
            }
        }

        if (nvrDescriptorList_relay != null) {
//            mStagesQueue.offer(new FotaStage_ResumeDspRelay(this));
            mStagesQueue.offer( new FotaStage_05_DetachResetRelay(this));
        }

        if (nvrDescriptorList != null) {
//            mStagesQueue.offer(new FotaStage_ResumeDsp(this));
            mStagesQueue.offer( new FotaStage_05_DetachReset(this));
        }

        startPollStagetQueue();
    }
}
